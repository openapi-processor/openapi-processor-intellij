/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiLiteralUtil

class MicronautAnnotation(
    override val name: String,
    override val method: String = name.lowercase()
): Annotation {
    override val pkg: String = "io.micronaut.http.annotation"

    override fun path(psi: PsiAnnotation): String? {
        val value = psi.findAttributeValue("uri")
        if (value !is PsiLiteralExpression)
            return null

        return PsiLiteralUtil.getStringLiteralContent(value)
    }

    override fun matches(psi: PsiAnnotation): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val value = psi.findAttributeValue("uri")
        if (value !is PsiLiteralExpression)
            return false

        return true
    }

    override fun matches(psi: PsiAnnotation, path: String): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val value = psi.findAttributeValue("uri")
        if (value !is PsiLiteralExpression)
            return false

        val uri = PsiLiteralUtil.getStringLiteralContent(value)
        if (uri != path)
            return false

        return true
    }
}
