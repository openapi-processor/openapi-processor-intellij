/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.*
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.runInEdtAndWait
import io.openapiprocessor.intellij.support.TargetPackageServiceStub
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.getTargets
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/package-name")
class TypeMappingPackageLineMarkerSpec {
    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")
    val sourceRootFixture = moduleFixture.sourceRootFixture(pathFixture = tempPathFixture)
    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)
    val disposableFixture = disposableFixture()

    val expectedPkgDir = "io/openapiprocessor"

    @BeforeEach
    fun stubService() {
        val fixture = codeInsightFixture.get()
        val module = moduleFixture.get()
        val baseDir = tempPathFixture.get()
        val virtualBaseDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(baseDir)!!

        val psiDir = runWriteAction {
            val generated = VfsTestUtil.createDir(virtualBaseDir, "src/generated/java")
            val model = ModuleRootManager.getInstance(module).modifiableModel

            try {
                val contentEntry = model.contentEntries.firstOrNull()
                    ?: model.addContentEntry(virtualBaseDir)

                contentEntry.addSourceFolder(generated, true)
                model.commit()
            } catch (e: Throwable) {
                model.dispose()
                throw e
            }

            val pkgDir = VfsTestUtil.createDir(generated, expectedPkgDir)
            fixture.psiManager.findDirectory(pkgDir)
        }

        ApplicationManager.getApplication().replaceService(
            TargetPackageService::class.java,
            TargetPackageServiceStub(psiDir),
            disposableFixture.get())
    }

    @Test
    fun `adds navigation gutter at package-name`() {
        val fixture = codeInsightFixture.get()
        fixture.copyDirectoryToProject("api", "")

        runInEdtAndWait {
            fixture.configureByFile("api/mapping-package-name.yaml")

            val gutters = fixture.findAllGutters()
            val gutter = gutters.first()

            assertEquals(AllIcons.Modules.GeneratedFolder, gutter.icon)
            assertEquals(TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT, gutter.tooltipText)
            assert(getTargets(gutter).first().endsWith(expectedPkgDir))
        }
    }

    @Test
    fun `adds navigation gutter at package-names base and package-names location`() {
        val fixture = codeInsightFixture.get()
        fixture.copyDirectoryToProject("api", "")

        runInEdt {
            fixture.configureByFile("api/mapping-package-names.yaml")

            val gutters = fixture.findAllGutters()
            assertEquals(2, gutters.size)

            val gutter0 = gutters[0]
            assertEquals(AllIcons.Modules.GeneratedFolder, gutter0.icon)
            assertEquals(TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT, gutter0.tooltipText)
            assert(getTargets(gutter0).first().endsWith(expectedPkgDir))

            val gutter1 = gutters[1]
            assertEquals(AllIcons.Modules.GeneratedFolder, gutter1.icon)
            assertEquals(TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT, gutter1.tooltipText)
            assert(getTargets(gutter1).first().endsWith(expectedPkgDir))
        }
    }
}
