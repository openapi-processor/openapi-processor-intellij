/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.icons.AllIcons
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.openapiprocessor.intellij.support.HeavyBaseTestCase
import io.openapiprocessor.intellij.support.TargetPackageFinderStub
import io.openapiprocessor.intellij.support.targets
import org.junit.Ignore

@Ignore // broken
class LineMarker2Test : HeavyBaseTestCase() {
    private var fixture: CodeInsightTestFixture? = null

    override fun setUp() {
        super.setUp()

        val builder = IdeaTestFixtureFactory
            .getFixtureFactory()
            .createFixtureBuilder(name)

        fixture = IdeaTestFixtureFactory
            .getFixtureFactory()
            .createCodeInsightFixture(builder.fixture)

        fixture?.setUp()
    }

    override fun tearDown() {
        fixture?.tearDown()
        super.tearDown()
    }

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

        fixture?.configureByText(
            "mapping.yaml", """
                openapi-processor-mapping: v2
                options:
                  package-name: io.openapiprocessor
            """.trimIndent())

        val expectedPkg = tmpDir.findFileByRelativePath("build/openapi/io/openapiprocessor")!!
        stubTargetPackageService(expectedPkg)

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait { gutters = fixture!!.findAllGutters("mapping.yaml") }

        // then
        val gutter = gutters.first()
        assertEquals(AllIcons.Modules.GeneratedFolder, gutter.icon)
        assertEquals(TypeMappingLineMarker.PACKAGE_EXISTS_TOOLTIP_TEXT, gutter.tooltipText)

        assertEquals(expectedPkg.path, gutter.targets.first())
    }

    private fun stubTargetPackageService(pkg: VirtualFile?) {
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
