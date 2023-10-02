/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.methods

class PathLineMarkerSpec: StringSpec({
    val test = register(LightCodeInsightListener())

    fun fixture(): CodeInsightTestFixture {
        return test.fixture!!
    }

    fun getMethod(name: String): PsiMethod {
        return JavaMethodNameIndex.getInstance()
            .get(name, fixture().project, GlobalSearchScope.allScope(fixture().project))
            .first()
    }


    beforeTest {
        fixture().testDataPath = "src/test/testdata/path-to-methods"
    }

    "test adds navigation gutter icon to micronaut interface methods" {
        var gutters: List<GutterMark> = emptyList()

        runInEdtAndWait {
            fixture().copyDirectoryToProject("micronaut", "")
            fixture().configureByFile("mapping.yaml")

            gutters = fixture().findAllGutters("mapping.yaml")
        }

        runReadAction {
            val expected = listOf(
                getMethod("deleteFoo"),
                getMethod("getFoo"),
                getMethod("headFoo"),
                getMethod("patchFoo"),
                getMethod("postFoo"),
                getMethod("putFoo"),
                getMethod("traceFoo")
            )

            val gutter = gutters.first {
                it.icon == TypeMappingPathLineMarker.Support.ICON
            }

            gutter.tooltipText shouldBe TypeMappingPathLineMarker.TOOLTIP_TEXT
            gutter.methods shouldContainExactly expected
        }
    }

    "test adds navigation gutter icon to spring interface methods" {
        var gutters: List<GutterMark> = emptyList()

        runInEdtAndWait {
            fixture().copyDirectoryToProject("spring", "")
            fixture().configureByFile("mapping.yaml")

            gutters = fixture().findAllGutters("mapping.yaml")
        }

        runReadAction {
            val expected = listOf(
                getMethod("deleteFoo"),
                getMethod("getFoo"),
                getMethod("headFoo"),
                getMethod("patchFoo"),
                getMethod("postFoo"),
                getMethod("putFoo"),
                getMethod("traceFoo")
            )

            val gutter = gutters.first {
                it.icon == TypeMappingPathLineMarker.Support.ICON
            }

            gutter.tooltipText shouldBe TypeMappingPathLineMarker.TOOLTIP_TEXT
            gutter.methods shouldContainExactly expected
        }
    }
})
