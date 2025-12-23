/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.TestApplicationManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.TestDataProvider
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestContext
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.annotations.TestOnly
import java.nio.file.Path

fun TestFixture<PsiDirectory>.virtualDirFixture(path: String): TestFixture<VirtualFile> {
    return testFixture {
        val parent = this@virtualDirFixture.init()

        val dir = edtWriteAction {
            VfsUtil.createDirectoryIfMissing(parent.virtualFile, path)
        }

        initialized(dir) {
          edtWriteAction {
            dir.delete(parent)
          }
        }
    }
}

// based on  com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture


@Target(AnnotationTarget.FUNCTION)
annotation class TestSubPath(val value: String)

@TestOnly
fun codeInsightFixture(
  projectFixture: TestFixture<Project>,
  tempDirFixture: TestFixture<Path>,
): TestFixture<CodeInsightTestFixture> = codeInsightFixture(
  projectFixture,
  tempDirFixture
) { project, tempDir -> CodeInsightTestFixtureImpl(project, tempDir) }


fun <T: CodeInsightTestFixture> codeInsightFixture(
  projectFixture: TestFixture<Project>,
  tempDirFixture: TestFixture<Path>,
  createFixture: (IdeaProjectTestFixture, TempDirTestFixture) -> T,
): TestFixture<T> = testFixture { context ->
  val project = projectFixture.init()
  val tempDir = tempDirFixture.init()

  val projectFixture = object : IdeaProjectTestFixture {
    override fun getProject(): Project = project

    override fun getModule(): Module {
      check(project.modules.isNotEmpty()) {
        "At least one module is required for the project. Use TestFixture<Project>.moduleFixture() to register one in your test class."
      }
      return project.modules[0]
    }

    override fun setUp() {
      TestApplicationManager.getInstance().setDataProvider(TestDataProvider(project))
    }

    override fun tearDown() {
      TestApplicationManager.getInstance().setDataProvider(null)
    }
  }
  val tempDirFixture = object : TempDirTestFixtureImpl() {
    // This method affects the internal temp dir used by the fixture, so we need to override it and not #getTempDir().
    override fun doCreateTempDirectory(): Path = tempDir

    // As the temporary directory is created by the external fixture, we don't need to handle it here.
    override fun deleteOnTearDown(): Boolean = false
  }

  val codeInsightFixture = createFixture(projectFixture, tempDirFixture)

  codeInsightFixture.testDataPath = getTestDataPath(context)

  ApplicationManager.getApplication().invokeAndWait {
    codeInsightFixture.setUp()
  }

  initialized(codeInsightFixture) {
    runInEdt {
      codeInsightFixture.tearDown()
    }
  }
}

private fun getTestDataPath(context: TestContext): String {
  val rootPath = context.findAnnotation(TestDataPath::class.java)?.value?.removePrefix($$"$PROJECT_ROOT/")
  check(rootPath != null) {
    "the test class should have a @TestDataPath annotation"
  }

//  val subPath = context.findAnnotation(TestSubPath::class.java)?.value ?: ""
  return rootPath

//  val homeDir = IdeaTestExecutionPolicy.getHomePathWithPolicy().toNioPathOrNull()
//  check(homeDir != null) {
//    "Couldn't create nio.Path from ${IdeaTestExecutionPolicy.getHomePathWithPolicy()}"
//  }
//  val resolvedPath = homeDir.resolve(rootPath).resolve(subPath)
//  if (resolvedPath.exists()) {
//   return resolvedPath.pathString
//  }
  // If the project opened as IJ community, then the test path for community will be duplicated, e.g. it is $HOME/community/community/...
  // To handle this scenario, we are trying to resolve the path $HOME/community/../community/...
//  check(rootPath.startsWith(COMMUNITY_PATH_PREFIX)) {
//    "The test data path is not located in community folder, but it doesn't exist in the ultimate."
//  }
//  return homeDir.resolve("../").resolve(rootPath).resolve(subPath).pathString
}
