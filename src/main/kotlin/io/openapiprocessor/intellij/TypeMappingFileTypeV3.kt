/*
 * Copyright 2022 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

class TypeMappingFileTypeV3: TypeMappingFileType() {
    override fun getVersion(): String {
        return "v3"
    }
}
