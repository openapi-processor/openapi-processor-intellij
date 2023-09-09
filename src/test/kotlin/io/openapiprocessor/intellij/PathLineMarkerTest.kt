/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.testFramework.runInEdtAndWait
import io.openapiprocessor.intellij.support.LightBaseTestCase
import io.openapiprocessor.intellij.support.methods

class PathLineMarkerTest  : LightBaseTestCase() {

    fun `test adds navigation gutter icon to micronaut interface methods`() {
        fixture.testDataPath = "src/test/testdata/path-to-methods"
        fixture.copyDirectoryToProject("micronaut", "")
        fixture.configureByFile("mapping.yaml")

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait {
            gutters = fixture.findAllGutters("mapping.yaml")
        }

        // then
        val gutter = gutters.first {
            it.icon == TypeMappingPathLineMarker.Support.ICON
        }

        val expected = listOf(
            getMethod("deleteFoo"),
            getMethod("getFoo"),
            getMethod("headFoo"),
            getMethod("patchFoo"),
            getMethod("postFoo"),
            getMethod("putFoo"),
            getMethod("traceFoo")
        )
        val methods = gutter.methods

        assertEquals(TypeMappingPathLineMarker.TOOLTIP_TEXT, gutter.tooltipText)
        assertEquals(expected.size, methods.size)
        assertEquals(expected, methods)
    }

    fun `test adds navigation gutter icon to spring interface methods`() {
        fixture.testDataPath = "src/test/testdata/path-to-methods"
        fixture.copyDirectoryToProject("spring", "")
        fixture.configureByFile("mapping.yaml")

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait {
            gutters = fixture.findAllGutters("mapping.yaml")
        }

        // then
        val gutter = gutters.first {
            it.icon == TypeMappingPathLineMarker.Support.ICON
        }

        val expected = listOf(
            getMethod("deleteFoo"),
            getMethod("getFoo"),
            getMethod("headFoo"),
            getMethod("patchFoo"),
            getMethod("postFoo"),
            getMethod("putFoo"),
            getMethod("traceFoo")
        )
        val methods = gutter.methods

        assertEquals(TypeMappingPathLineMarker.TOOLTIP_TEXT, gutter.tooltipText)
        assertEquals(expected.size, methods.size)
        assertEquals(expected, methods)
    }

}
