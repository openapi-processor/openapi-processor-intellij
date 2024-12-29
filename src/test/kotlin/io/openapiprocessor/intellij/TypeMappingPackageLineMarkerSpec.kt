/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.TargetPackageFinderStub
import io.openapiprocessor.intellij.support.getTargets

class TypeMappingPackageLineMarkerSpec: StringSpec({
    val test = register(LightCodeInsightListener())

    fun fixture(): CodeInsightTestFixture {
        return test.fixture!!
    }

    beforeTest {
        fixture().testDataPath = "src/test/testdata/package-name"
    }


    fun stubTargetPackageService(pkg: VirtualFile?) {
        runReadAction {
            val psiDir: PsiDirectory? = if (pkg != null) {
                fixture().psiManager.findDirectory(pkg)
            } else {
                null
            }

            fixture().project.replaceService(
                TargetPackageService::class.java,
                TargetPackageService(TargetPackageFinderStub(psiDir)),
                fixture().testRootDisposable
            )
        }
    }

    "test adds navigation gutter at package-name if it exists" {
        val tmpDir = directoryContent {
            dir("build") {
                dir("openapi") {
                    dir("io") {
                        dir("openapiprocessor") {}
                    }
                }
            }
        }.generateInVirtualTempDir()

        val expectedPkg = tmpDir.findFileByRelativePath("build/openapi/io/openapiprocessor")!!
        stubTargetPackageService(expectedPkg)

        runInEdtAndWait {
            val gutters = fixture().findAllGutters("api/mapping.yaml")
            val gutter = gutters.first()

            gutter.icon shouldBe AllIcons.Modules.GeneratedFolder
            gutter.tooltipText shouldBe TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT

            getTargets(gutter).first() shouldBe expectedPkg.path
        }
    }

    "test adds 'empty' navigation gutter at package-name if it does not exist" {
        directoryContent {
            dir("build") {
                dir("openapi") {
                }
            }
        }.generateInVirtualTempDir()

        stubTargetPackageService(null)

        // when
        runInEdtAndWait {
            val gutters = fixture().findAllGutters("api/mapping.yaml")
            val gutter = gutters.first()

            gutter.icon shouldBe AllIcons.Modules.GeneratedFolder
            gutter.tooltipText shouldBe TypeMappingLineMarker.PACKAGE_MISSING_TOOLTIP_TEXT
            getTargets(gutter).shouldBeEmpty()
        }
    }
})
