/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.icons.AllIcons
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.openapiprocessor.intellij.support.LightBaseTestCase
import io.openapiprocessor.intellij.support.TargetPackageFinderStub
import io.openapiprocessor.intellij.support.targets

class LineMarkerTest: LightBaseTestCase() {

    fun `test adds navigation gutter at package-name if it exists`() {
        val tmpDir = directoryContent {
            dir("build") {
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

        val expectedPkg = tmpDir.findFileByRelativePath("build/openapi/io/openapiprocessor")!!
        stubTargetPackageService(expectedPkg)

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait { gutters = fixture.findAllGutters("mapping.yaml") }

        // then
        val gutter = gutters.first()
        assertEquals(AllIcons.Modules.GeneratedFolder, gutter.icon)
        assertEquals(TypeMappingLineMarker.PACKAGE_EXISTS_TOOLTIP_TEXT, gutter.tooltipText)

        assertEquals(expectedPkg.path, gutter.targets.first())
    }

    fun `test adds 'empty' navigation gutter at package-name if it does not exist`() {
        val tmpDir = directoryContent {
            dir("build") {
                dir("openapi") {
                }
            }
        }.generateInVirtualTempDir()

        val mapping = fixture.configureByText(
            "mapping.yaml", """
                openapi-processor-mapping: v2
                options:
                  package-name: io.openapiprocessor
            """.trimIndent())

        stubTargetPackageService(null)

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait { gutters = fixture.findAllGutters("mapping.yaml") }

        // then
        val gutter = gutters.first()
        assertEquals(AllIcons.Modules.GeneratedFolder, gutter.icon)
        assertEquals(TypeMappingLineMarker.PACKAGE_MISSING_TOOLTIP_TEXT, gutter.tooltipText)
        assertEquals(0, gutter.targets.size)
    }

    private fun stubTargetPackageService(pkg: VirtualFile?)  {
        val psiDir: PsiDirectory? = if (pkg != null) {
            psiManager.findDirectory(pkg)
        } else {
            null
        }

        project.replaceService(
            TargetPackageService::class.java,
            TargetPackageService(TargetPackageFinderStub(psiDir)),
            testRootDisposable)
    }

}

