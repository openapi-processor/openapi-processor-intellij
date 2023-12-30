/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

object Annotations {
    val KNOWN = listOf(
        MicronautAnnotation("Delete"),
        MicronautAnnotation("Get"),
        MicronautAnnotation("Head"),
        MicronautAnnotation("Patch"),
        MicronautAnnotation("Post"),
        MicronautAnnotation("Put"),
        MicronautAnnotation("Trace"),

        SpringAnnotation("DeleteMapping", "delete"),
        SpringAnnotation("GetMapping", "get"),
        SpringRequestAnnotation("HEAD"),
        SpringAnnotation("PatchMapping", "patch"),
        SpringAnnotation("PostMapping", "post"),
        SpringAnnotation("PutMapping", "put"),
        SpringRequestAnnotation("TRACE")
    )

    fun withMethod(method: String): List<Annotation> {
        return KNOWN.filter {
            it.method == method
        }
    }
}
