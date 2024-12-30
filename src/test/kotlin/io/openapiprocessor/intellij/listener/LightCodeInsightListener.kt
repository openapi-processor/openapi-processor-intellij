/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.listener

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.intellij.testFramework.replaceService
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class LightCodeInsightListener(private var testDataPath: String? = null) : TestListener {
    var fixture: CodeInsightTestFixture? = null
    private var tmpDirFixture: TempDirTestFixture? = null

    override suspend fun beforeTest(testCase: TestCase) {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder = factory.createLightFixtureBuilder(testCase.name.testName)
        tmpDirFixture = LightTempDirTestFixtureImpl(true)

        fixture = factory.createCodeInsightFixture(builder.fixture, tmpDirFixture!!)
        if (testDataPath != null) {
            fixture!!.testDataPath = testDataPath!!
        }

        fixture!!.setUp()
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        fixture!!.tearDown()
    }

    val project get() = fixture!!.project!!

    fun createDir(path: String): VirtualFile {
        return VfsTestUtil.createDir(LightPlatformTestCase.getSourceRoot(), path)
    }

    fun findPsiDir(path: VirtualFile): PsiDirectory {
        return fixture!!.psiManager.findDirectory(path)!!
    }

    fun copyFileToProject(@TestDataFile sourceFilePath: String): VirtualFile {
        return fixture!!.copyFileToProject(sourceFilePath)
    }

    fun copyDirectoryToProject(@TestDataFile sourceFilePath: String, targetPath: String): VirtualFile {
        return fixture!!.copyDirectoryToProject(sourceFilePath, targetPath)
    }

    fun configureByFile(@TestDataFile sourceFilePath: String): PsiFile {
        return fixture!!.configureByFile(sourceFilePath)
    }

    fun configureFromTempProjectFile(sourceFilePath: String): PsiFile {
        return fixture!!.configureFromTempProjectFile(sourceFilePath)
    }

    fun <T : Any> replaceService(serviceInterface: Class<T>, instance: T) {
        ApplicationManager.getApplication().replaceService(serviceInterface, instance, fixture!!.testRootDisposable)
    }

    fun findAllGutters(@TestDataFile sourceFilePath: String): List<GutterMark> {
        return fixture!!.findAllGutters(sourceFilePath)
    }
}
