/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiLiteralUtil

class SpringRequestAnnotation(override val method: String): Annotation {
    override val pkg: String = "org.springframework.web.bind.annotation"
    override val name: String = "RequestMapping"

    override fun matches(psi: PsiAnnotation, path: String): Boolean {
        if (!psi.hasQualifiedName(qualifiedName))
            return false

        val methodRef = psi.findAttributeValue("method")
        if (methodRef !is PsiReferenceExpression)
            return false

        if ( methodRef.qualifiedName != "RequestMethod.${method.uppercase()}")
            return false

        val value = psi.findAttributeValue("path")
        if (value !is PsiLiteralExpression)
            return false

        val uri = PsiLiteralUtil.getStringLiteralContent(value)
        if (uri != path)
            return false

        return true
    }
}
