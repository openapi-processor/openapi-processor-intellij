/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.utils.vfs.refreshAndGetVirtualDirectory
import com.intellij.util.io.directoryContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.absolute

@TestApplication
class TargetPackageFinderSpec {
    val projectFixture = projectFixture()

    val moduleFixture = testFixture {
        val project = projectFixture.init()

        val path = Path(project.basePath!!).absolute()

        directoryContent {
            dir("build") {
                dir("out") {
                    dir("main") {}
                    dir("test") {}
                }
                dir("openapi") {
                    dir("io") {
                        dir("openapiprocessor") {}
                    }
                }
            }
            dir("src") {
                dir("api") {
                    file("mapping.yaml", """
                            openapi-processor-mapping: v0
                            options:
                              package-name: io.openapiprocessor
                            """.trimIndent())
                }
                dir("main") {
                    dir("io") {
                        dir("openapiprocessor") {}
                    }
                }
                dir("test") {
                    dir("io") {
                        dir("openapiprocessor") {}
                    }
                }
            }
        }.generate(path)

        val mappingModule = runWriteActionAndWait {
            // main module
            val main = path.resolve("src/main").refreshAndGetVirtualDirectory()
            val mainModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "main", main)
            val mainContent = PsiTestUtil.addContentRoot(mainModule, main)
            val mainSource = PsiTestUtil.addSourceRoot(mainModule, main)
            val mainOut = path.resolve("build/out/main").refreshAndGetVirtualDirectory()
            PsiTestUtil.setCompilerOutputPath(mainModule, mainOut.path, false)

            // api module
            val api = path.resolve("src/api").refreshAndGetVirtualDirectory()
            val apiModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "api", api)
            val apiContent = PsiTestUtil.addContentRoot(apiModule, api)
            val apiSource = PsiTestUtil.addSourceRoot(apiModule, api)
            // api module output is an additional source root of the main module
            val apiOut = path.resolve("build/openapi").refreshAndGetVirtualDirectory()
            PsiTestUtil.setCompilerOutputPath(apiModule, apiOut.path, false)
            PsiTestUtil.addSourceRoot(mainModule, apiOut)

            // test module
            val test = path.resolve("src/test").refreshAndGetVirtualDirectory()
            val testModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "test", test)
            val testContent = PsiTestUtil.addContentRoot(testModule, test)
            val testSource = PsiTestUtil.addSourceRoot(testModule, test)
            val testOut = path.resolve("build/out/test").refreshAndGetVirtualDirectory()
            PsiTestUtil.setCompilerOutputPath(testModule, testOut.path, true)

            val virtualFilePath = path.refreshAndGetVirtualDirectory()
            val mapping = VfsUtil.findRelativeFile(virtualFilePath, "src", "api", "mapping.yaml")!!

            ModuleUtil.findModuleForFile(mapping, project)
        }

        initialized(mappingModule!!) {}
    }

    fun actualDirs(dirs: List<PsiDirectory>): List<String> {
        val basePathLength = projectFixture.get().basePath!!.length + 1
        return dirs
            .map { it.virtualFile.path.substring(basePathLength) }
            .sorted()
    }

    @Test
    fun `find target package directories`() {
        val service = service<TargetPackageService>()

        val pkgDirs = ReadAction.compute<List<PsiDirectory>, Throwable> {
            service.findPackageDirs("io.openapiprocessor", moduleFixture.get())
        }

        assertEquals(3, pkgDirs.size)
        assertEquals(
            listOf(
                "src/main/io/openapiprocessor",
                "src/test/io/openapiprocessor",
                "build/openapi/io/openapiprocessor")
                .sorted(),
            actualDirs(pkgDirs))
    }
}

