/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runInEdt
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.methods
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/path-to-methods")
class TypeMappingPathLineMarkerSpec {
    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")
    val sourceRootFixture = moduleFixture.sourceRootFixture(pathFixture = tempPathFixture)
    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)

    fun getMethod(name: String): PsiMethod {
        val project = projectFixture.get()

        return JavaMethodNameIndex
            .getInstance()
            .getMethods(name, project, GlobalSearchScope.allScope(project))
            .first()
    }

    @Test
    fun `adds navigation gutter icon to micronaut interface methods`() {
        val fixture = codeInsightFixture.get()

        runInEdt {
            fixture.copyDirectoryToProject("micronaut", "")
            fixture.configureByFile("mapping.yaml")

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

            assertEquals(TypeMappingPathLineMarker.Icon.`interface`, gutter.icon)
            assertEquals(TypeMappingPathLineMarker.I18n.TOOLTIP_TEXT, gutter.tooltipText)
            assertEquals(expected, gutter.methods)
        }
    }

    @Test
    fun `adds navigation gutter icon to spring interface methods`() {
        val fixture = codeInsightFixture.get()

        runInEdt {
            fixture.copyDirectoryToProject("spring", "")
            fixture.configureByFile("mapping.yaml")

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

            assertEquals(TypeMappingPathLineMarker.Icon.`interface`, gutter.icon)
            assertEquals(TypeMappingPathLineMarker.I18n.TOOLTIP_TEXT, gutter.tooltipText)
            assertEquals(expected, gutter.methods)
        }
    }
}
