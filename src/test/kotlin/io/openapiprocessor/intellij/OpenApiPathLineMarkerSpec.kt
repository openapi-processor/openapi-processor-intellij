/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.methods
import io.openapiprocessor.intellij.support.testDataSourceRootFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@RunInEdt
@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/openapi-to-interface/paths")
class OpenApiPathLineMarkerSpec {
    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")
    val sourceRootFixture = moduleFixture.testDataSourceRootFixture(tempPathFixture)
    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)

    fun getMethod(name: String): PsiMethod = runReadActionBlocking {
        val project = projectFixture.get()

        JavaMethodNameIndex
            .getInstance()
            .getMethods(name, project, GlobalSearchScope.allScope(project))
            .first()
    }

    @Test
    fun `adds navigation gutter icon to OpenAPI paths`() {
        val fixture = codeInsightFixture.get()
        fixture.configureByFile("openapi.yaml")

        val gutters = fixture.findAllGutters()

        val expected = listOf(
            getMethod("postFoo"),
            getMethod("postBar")
        )

        val methods = gutters.flatMap { it.methods }

        assertEquals(expected, methods)
    }
}
