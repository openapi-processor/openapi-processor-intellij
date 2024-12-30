/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.EDT
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.psiTargets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MappingAnnotationLineMarkerSpec : StringSpec({
    val fixture = register(LightCodeInsightListener("src/test/testdata/interface-to-openapi/paths"))

    "adds navigation gutter icon to mapping annotation" {
        withContext(Dispatchers.EDT) {
            fixture.copyDirectoryToProject("", "")

            val gutters = fixture.findAllGutters("api/Api.java")
            gutters.size shouldBe 2

            val bar = gutters[0]
            bar.icon shouldBe MappingAnnotationLineMarker.Icon.openapi
            bar.tooltipText shouldBe MappingAnnotationLineMarker.I18n.TOOLTIP_TEXT
            val barTargets = bar.psiTargets.first()
            barTargets.text shouldBe "/bar"

            val foo = gutters[1]
            bar.icon shouldBe MappingAnnotationLineMarker.Icon.openapi
            bar.tooltipText shouldBe MappingAnnotationLineMarker.I18n.TOOLTIP_TEXT
            val fooTargets = foo.psiTargets.first()
            fooTargets.text shouldBe "/foo"
        }
    }
})
