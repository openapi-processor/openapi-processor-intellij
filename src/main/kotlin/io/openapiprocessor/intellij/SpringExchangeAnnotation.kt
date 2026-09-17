/*
 * Copyright 2026 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiLiteralUtil

// org.springframework.web.service.annotation.${methodName}Exchange

class SpringExchangeAnnotation(override val name: String, override val method: String): Annotation {
    override val pkg: String = "org.springframework.web.service.annotation"

    override fun path(psi: PsiAnnotation): String? {
        val value = psi.findAttributeValue("url")
        if (value !is PsiLiteralExpression)
            return null

        return PsiLiteralUtil.getStringLiteralContent(value)
    }

    override fun matches(psi: PsiAnnotation): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val value = psi.findAttributeValue("url")
        return value is PsiLiteralExpression
    }

    override fun matches(psi: PsiAnnotation, path: String): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val value = psi.findAttributeValue("url")
        if (value !is PsiLiteralExpression)
            return false

        val uri = PsiLiteralUtil.getStringLiteralContent(value)
        return uri == path
    }
}
