/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.EDT
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.methods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TypeMappingPathLineMarkerSpec: StringSpec({
    isolationMode = IsolationMode.SingleInstance

    val fixture = extension(LightCodeInsightListener("src/test/testdata/path-to-methods"))

    fun getMethod(name: String): PsiMethod {
        return JavaMethodNameIndex
            .getInstance()
            .getMethods(name, fixture.project, GlobalSearchScope.allScope(fixture.project))
            .first()
    }

    "adds navigation gutter icon to micronaut interface methods" {
        withContext(Dispatchers.EDT) {
            fixture.copyDirectoryToProject("micronaut", "")

            val expected = listOf(
                getMethod("deleteFoo"),
                getMethod("getFoo"),
                getMethod("headFoo"),
                getMethod("patchFoo"),
                getMethod("postFoo"),
                getMethod("putFoo"),
                getMethod("traceFoo"))

            val gutters = fixture.findAllGutters("mapping.yaml")
            val gutter = gutters[1] // skip package-name gutter

            gutter.icon shouldBe TypeMappingPathLineMarker.Icon.`interface`
            gutter.tooltipText shouldBe TypeMappingPathLineMarker.I18n.TOOLTIP_TEXT
            gutter.methods shouldContainExactly expected
        }
    }

    "test adds navigation gutter icon to spring interface methods" {
        withContext(Dispatchers.EDT) {
            fixture.copyDirectoryToProject("spring", "")

            val expected = listOf(
                getMethod("deleteFoo"),
                getMethod("getFoo"),
                getMethod("headFoo"),
                getMethod("patchFoo"),
                getMethod("postFoo"),
                getMethod("putFoo"),
                getMethod("traceFoo"))

            val gutters = fixture.findAllGutters("mapping.yaml")
            val gutter = gutters[1] // skip package-name gutter

            gutter.icon shouldBe TypeMappingPathLineMarker.Icon.`interface`
            gutter.tooltipText shouldBe TypeMappingPathLineMarker.I18n.TOOLTIP_TEXT
            gutter.methods shouldContainExactly expected
        }
    }
})
