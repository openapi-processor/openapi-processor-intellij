/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.openapiprocessor.intellij.support.targets

class LineMarkerTest: BaseTestCase() {

    fun `test adds navigation gutter at package-name if it exists`() {
        val tmpDir = directoryContent {
            dir("build") {
                dir("compilerOutput") {}
                dir("openapi") {
                    dir("io") {
                        dir("openapiprocessor") {}
                    }
                }
            }
        }.generateInVirtualTempDir()

        val mapping = fixture.configureByText(
            "mapping.yaml", """
                openapi-processor-mapping: v2
                options:
                  package-name: io.openapiprocessor
            """.trimIndent())

        val compilerOutput = tmpDir.findFileByRelativePath("build/compilerOutput")!!
        setCompilerOutputPath(compilerOutput)

        // root of "generated" code
        val contentRoot = tmpDir.findFileByRelativePath("build/openapi")!!
        addContentRoot(contentRoot)

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait { gutters = fixture.findAllGutters("mapping.yaml") }

        // then
        val gutter = gutters.first()
        assertEquals(AllIcons.Modules.GeneratedFolder, gutter.icon)
        assertEquals(TypeMappingLineMarker.PACKAGE_TOOLTIP_TEXT, gutter.tooltipText)

        val pkg = tmpDir.findFileByRelativePath("build/openapi/io/openapiprocessor")!!
        assertEquals(pkg.path, gutter.targets.first())
    }

    private fun addContentRoot(api: VirtualFile) {
        val cnt = PsiTestUtil.addContentRoot(module, api)
        val src = PsiTestUtil.addSourceRoot(module, api)

        Disposer.register(testRootDisposable) {
            PsiTestUtil.removeContentEntry(module, cnt.file!!)
        }
    }

    private fun setCompilerOutputPath(build: VirtualFile) {
        PsiTestUtil.setCompilerOutputPath(fixture.module, build.path, false)

        Disposer.register(testRootDisposable) {
            PsiTestUtil.setCompilerOutputPath(module, "", false)
        }
    }

}

