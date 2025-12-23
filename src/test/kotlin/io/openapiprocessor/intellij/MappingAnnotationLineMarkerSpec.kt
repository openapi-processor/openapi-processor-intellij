/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.*
import com.intellij.testFramework.replaceService
import io.openapiprocessor.intellij.support.ModuleServiceStub
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.psiTargets
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/interface-to-openapi/paths")
class MappingAnnotationLineMarkerSpec {

    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")

    val sourceRootFixture = moduleFixture.sourceRootFixture(
        pathFixture = tempPathFixture,
        blueprintResourcePath = Path.of("src/test/testdata/interface-to-openapi/paths")
    )

    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)
    val disposableFixture = disposableFixture()


    @BeforeEach
    fun stubModuleService() {
        ApplicationManager.getApplication().replaceService(
            ModuleService::class.java,
            ModuleServiceStub(),
            disposableFixture.get()
        )
    }

    @Test
    fun `adds navigation gutter icon to mapping annotation`() {
        val fixture = codeInsightFixture.get()

        val file = VfsUtil.findRelativeFile(sourceRootFixture.get().virtualFile, "api", "Api.java")!!
        fixture.configureFromExistingVirtualFile(file)

        val gutters = fixture.findAllGutters("api/Api.java")
        assertEquals(2, gutters.size)

        runReadAction {
            val bar = gutters[0]
            assertEquals(MappingAnnotationLineMarker.Icon.openapi, bar.icon)
            assertEquals(MappingAnnotationLineMarker.I18n.TOOLTIP_TEXT, bar.tooltipText)
            assertEquals(bar.psiTargets.first().text, "/bar")

            val foo = gutters[1]
            assertEquals(MappingAnnotationLineMarker.Icon.openapi, foo.icon)
            assertEquals(MappingAnnotationLineMarker.I18n.TOOLTIP_TEXT, foo.tooltipText)
            assertEquals(foo.psiTargets.first().text, "/foo")
        }
    }
}
