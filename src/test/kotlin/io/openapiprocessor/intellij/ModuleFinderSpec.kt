/*
 * Copyright 2026 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.ModuleType
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.utils.vfs.refreshAndGetVirtualDirectory
import com.intellij.util.io.directoryContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.absolute

@TestApplication
class ModuleFinderSpec {
    val projectFixture = projectFixture()

    val modulesFixture = testFixture {
        val project = projectFixture.init()
        val path = Path(project.basePath!!).absolute()

        directoryContent {
            dir("src") {
                dir("api") {
                    file("mapping.yaml", "")
                }
                dir("main") {
                    dir("kotlin") {
                        dir("io") {
                            dir("openapiprocessor") {
                                file("Api.kt", "")
                            }
                        }
                    }
                }
                dir("test") {
                    dir("kotlin") {
                        dir("io") {
                            dir("openapiprocessor") {
                                file("ApiSpec.kt", "")
                            }
                        }
                    }
                }
            }
        }.generate(path)

        val modules = runWriteActionAndWait {
            val contentDir = path.refreshAndGetVirtualDirectory()
            val apiDir = path.resolve("src/api").refreshAndGetVirtualDirectory()
            val mainSourceDir = path.resolve("src/main/kotlin").refreshAndGetVirtualDirectory()
            val testSourceDir = path.resolve("src/test/kotlin").refreshAndGetVirtualDirectory()

            // api module
            val apiModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "api", contentDir)
            PsiTestUtil.addSourceRoot(apiModule, apiDir)

            // main module
            val mainModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "main", contentDir)
            PsiTestUtil.addSourceRoot(mainModule, mainSourceDir)

            // test module
            val testModule = PsiTestUtil.addModule(project, ModuleType.EMPTY, "test", contentDir)
            PsiTestUtil.addSourceRoot(testModule, testSourceDir)

            listOf(apiModule, mainModule, testModule)
        }

        initialized(modules) {}
    }

    @Test
    fun `finds sibling modules when querying with a source file inside one of the modules`() {
        val project = projectFixture.get()
        modulesFixture.get()

        val apiFile = Path(project.basePath!!)
            .resolve("src/api/mapping.yaml")
            .absolute()
            .toString()

        val finder = ModuleFinder(project)
        val modules = finder.findModules(apiFile)
        val names = modules.map { it.name }.sorted()

        assertEquals(3, modules.size)
        assertEquals(listOf("api", "main", "test"), names)
    }

    @Test
    fun `returns empty list when no module source root matches the given file path`() {
        val project = projectFixture.get()
        modulesFixture.get()

        val finder = ModuleFinder(project)
        val unknownFile = Path(project.basePath!!).resolve("other/unknown.yaml").absolute().toString()
        val modules = finder.findModules(unknownFile)

        assertTrue(modules.isEmpty())
    }
}
