/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.project.DumbService
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


@TestApplication
class AnnotationsSpec {

    companion object {
        val springPostAnnotation = SpringAnnotation("PostMapping", "post")
        val micronautPostAnnotation = MicronautAnnotation("Post", "post")

        val project by projectFixture()
    }

    @Test
    fun `post psi annotation matches fully qualified spring @PostMapping`() {
        DumbService.getInstance(project).runWhenSmart {
            val psiAnnotation = create("""@${springPostAnnotation.qualifiedName}(path = "/bar")""")
            assertTrue(springPostAnnotation.matches(psiAnnotation))
        }
    }

    @Test
    fun `post psi annotation matches fully qualified micronaut @Post`() {
        DumbService.getInstance(project).runWhenSmart {
            val psiAnnotation = create("""@${micronautPostAnnotation.qualifiedName}(path = "/bar")""")
            assertTrue(micronautPostAnnotation.matches(psiAnnotation))
        }
    }

    fun create(annotation: String, context: PsiElement? = null): PsiAnnotation {
        val elementFactory = JavaPsiFacade.getElementFactory(project)
        return elementFactory.createAnnotationFromText(annotation, context)
    }
}

