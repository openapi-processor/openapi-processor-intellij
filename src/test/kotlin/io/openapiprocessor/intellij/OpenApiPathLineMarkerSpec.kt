/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndGet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.methods

class OpenApiPathLineMarkerSpec : StringSpec({
    val test = extension(LightCodeInsightListener())

    fun fixture(): CodeInsightTestFixture {
        return test.fixture!!
    }

    beforeTest {
        fixture().testDataPath = "src/test/testdata/openapi-to-interface/paths"
    }

    fun getMethod(name: String): PsiMethod {
        return JavaMethodNameIndex
            .getInstance()
            .getMethods(name, fixture().project, GlobalSearchScope.allScope(fixture().project))
            .first()
    }

    "adds navigation gutter icon to OpenAPI paths" {
        val gutters = runInEdtAndGet {
            fixture().copyDirectoryToProject("api", "")
            fixture().copyDirectoryToProject("org", "")
            fixture().configureByFile("openapi.yaml")

            return@runInEdtAndGet fixture().findAllGutters("openapi.yaml")
        }

        runReadAction {
            val expected = listOf(
                getMethod("postFoo"),
                getMethod("postBar")
            )

            val methods = gutters
                .map { it.methods }
                .flatten()

            methods shouldContainExactly expected
        }
    }
})
