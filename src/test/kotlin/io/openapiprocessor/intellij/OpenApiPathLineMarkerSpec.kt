/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.*
import com.intellij.testFramework.runInEdtAndGet
import io.openapiprocessor.intellij.support.codeInsightFixture
import io.openapiprocessor.intellij.support.methods
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/src/test/testdata/openapi-to-interface/paths")
class OpenApiPathLineMarkerSpec {
    val tempPathFixture = tempPathFixture()
    val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)
    val moduleFixture = projectFixture.moduleFixture("main")

    val sourceRootFixture = moduleFixture.sourceRootFixture(
        pathFixture = tempPathFixture,
        blueprintResourcePath = Path.of("src/test/testdata/openapi-to-interface/paths")
    )

    val codeInsightFixture = codeInsightFixture(projectFixture, tempPathFixture)
    val disposableFixture = disposableFixture()

    fun getMethod(name: String): PsiMethod {
        val project = projectFixture.get()

        return JavaMethodNameIndex
            .getInstance()
            .getMethods(name, project, GlobalSearchScope.allScope(project))
            .first()
    }

    @Test
    fun `adds navigation gutter icon to OpenAPI paths`() {
        val fixture = codeInsightFixture.get()

        val gutters = runInEdtAndGet {
            val file = VfsUtil.findRelativeFile(sourceRootFixture.get().virtualFile, "openapi.yaml")!!
            fixture.configureFromExistingVirtualFile(file)

            return@runInEdtAndGet fixture.findAllGutters("openapi.yaml")
        }

        runReadAction {
            val expected = listOf(
                getMethod("postFoo"),
                getMethod("postBar")
            )

            val methods = gutters.flatMap { it.methods }

            assertEquals(expected, methods)
        }
    }
}
