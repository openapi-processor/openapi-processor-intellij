/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

class TypeMappingFileTypeV21: TypeMappingFileType() {
    override fun getVersion(): String {
        return "v2.1"
    }
}
