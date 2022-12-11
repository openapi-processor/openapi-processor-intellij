/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.targetpackagefinder

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PsiTestUtil
import com.intellij.util.io.directoryContent
import io.openapiprocessor.intellij.support.HeavyBaseTestCase
import java.io.File

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SimpleModules

class SimpleModulesFactory(private val testCase: HeavyBaseTestCase) {
    private val project: Project = testCase.project
    private val module: Module = testCase.module!!

    @Suppress("UNUSED_VARIABLE")
    fun setup() {
        val baseFile = File(project.basePath!!)

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
                    file("mapping.yaml",
                        """
                            openapi-processor-mapping: v2
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
        }.generate(baseFile)

        val base = testCase.initBase()

        runWriteAction {
            // project module
            val src = base.findFileByRelativePath("src")!!
            val rootContent = PsiTestUtil.addContentRoot(module, base)

            // other modules
            val main = base.findFileByRelativePath("src/main")!!
            val test = base.findFileByRelativePath("src/test")!!
            val api = base.findFileByRelativePath("build/openapi")!!

            // main module with processor target folder source root
            val mainModule = testCase.createEmptyModule("main", main.path)
            val mainContent = PsiTestUtil.addContentRoot(mainModule, main)
            val mainSource = PsiTestUtil.addSourceRoot(mainModule, main)
            val apiContent = PsiTestUtil.addSourceRoot(mainModule, api)
            val apiSource = PsiTestUtil.addSourceRoot(mainModule, api)
            val mainOut = base.findFileByRelativePath("build/out/main")!!
            PsiTestUtil.setCompilerOutputPath(mainModule, mainOut.path, false)

            // test module
            val testModule = testCase.createEmptyModule("test", test.path)
            val testContent = PsiTestUtil.addContentRoot(testModule, test)
            val testSource = PsiTestUtil.addSourceRoot(testModule, test)
            val testOut = base.findFileByRelativePath("build/out/test")!!
            PsiTestUtil.setCompilerOutputPath(testModule, testOut.path, true)
        }
    }

}
