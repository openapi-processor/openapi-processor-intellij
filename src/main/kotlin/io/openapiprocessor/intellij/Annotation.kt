/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.psi.PsiAnnotation

interface Annotation {
    val pkg: String
    val name: String
    val qualifiedName: String
        get() = "$pkg.$name"

    val method: String

    fun matches(psi: PsiAnnotation, path: String): Boolean
}
