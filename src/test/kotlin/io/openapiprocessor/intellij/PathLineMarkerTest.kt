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

    fun `test adds navigation gutter icon to interface methods`() {
        fixture.testDataPath = "src/test/testdata/path-to-methods"
        fixture.copyDirectoryToProject("", "")
        fixture.configureByFile("mapping.yaml")

        // when
        lateinit var gutters: List<GutterMark>
        runInEdtAndWait {
            gutters = fixture.findAllGutters("mapping.yaml")
        }

        // then
        val gutter = gutters.first {
            it.icon == TypeMappingPathLineMarker.ICON
        }

        val expected = listOf(
            getMethod("getFoo"),
            getMethod("postFoo")
        )
        val methods = gutter.methods

        assertEquals(TypeMappingPathLineMarker.TOOLTIP_TEXT, gutter.tooltipText)
        assertEquals(2, methods.size)
        assertEquals(expected, methods)
    }

}
