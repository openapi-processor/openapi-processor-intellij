/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

object Methods {
    private val methods = listOf("delete", "get", "head", "patch", "post", "put", "trace")

    fun isMethod(text: String): Boolean {
        return methods.contains(text)
    }
}
