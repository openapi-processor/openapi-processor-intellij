/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiLiteralUtil

class SpringHttpExchangeAnnotation(override val method: String): Annotation {
    override val pkg: String = "org.springframework.web.service.annotation"
    override val name: String = "HttpExchange"

    override fun path(psi: PsiAnnotation): String? {
        val value = psi.findAttributeValue("url")
        if (value !is PsiLiteralExpression)
            return null

        return PsiLiteralUtil.getStringLiteralContent(value)
    }

    override fun matches(psi: PsiAnnotation): Boolean {
        return false
    }

    override fun matches(psi: PsiAnnotation, path: String): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val methodRef = psi.findAttributeValue("method")
        if (methodRef !is PsiReferenceExpression)
            return false

        if ( methodRef.qualifiedName != "RequestMethod.${method.uppercase()}")
            return false

        val value = psi.findAttributeValue("url")
        if (value !is PsiLiteralExpression)
            return false

        val uri = PsiLiteralUtil.getStringLiteralContent(value)
        return uri == path
    }
}
