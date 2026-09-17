/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.disposableFixture
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.replaceService
import io.openapiprocessor.intellij.support.ModuleServiceStub
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.psiTargets
import io.openapiprocessor.intellij.support.testDataSourceRootFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@RunInEdt
@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/interface-to-openapi/paths")
class MappingAnnotationLineMarkerSpec {
    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")
    val sourceRootFixture = moduleFixture.testDataSourceRootFixture(tempPathFixture)
    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)
    val disposableFixture = disposableFixture()

    @BeforeEach
    fun stubModuleService() {
        ApplicationManager.getApplication().replaceService(
            ModuleService::class.java,
            ModuleServiceStub(),
            disposableFixture.get())
    }

    @Test
    fun `adds navigation gutter icon to mapping annotation`() {
        val fixture = codeInsightFixture.get()
        fixture.configureByFile("api/Api.java")

        val gutters = fixture.findAllGutters()

        assertEquals(3, gutters.size)
        assertGutter(gutters[0], "/bar")
        assertGutter(gutters[1], "/foo")
        assertGutter(gutters[2], "/ref/nested")
    }

    private fun assertGutter(gutter: GutterMark, expectedTarget: String) = runReadActionBlocking {
        assertEquals(MappingAnnotationLineMarker.Icon.openapi, gutter.icon)
        assertEquals(MappingAnnotationLineMarker.I18n.TOOLTIP_TEXT, gutter.tooltipText)
        assertEquals(expectedTarget, gutter.psiTargets.first().text)
    }
}
