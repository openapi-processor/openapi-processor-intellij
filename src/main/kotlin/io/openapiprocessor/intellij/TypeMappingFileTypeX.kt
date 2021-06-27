/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import org.yaml.snakeyaml.Yaml

class TypeMappingFileTypeX : TypeMappingFileType(), FileTypeIdentifiableByVirtualFile {

    override fun isMyFileType(file: VirtualFile): Boolean {
        if(!listOf("yaml", "yml").contains(file.extension)) {
            return false
        }

        if (!file.isInLocalFileSystem) {
            return false
        }

        val yaml = Yaml()
        val mapping: Map<String, Any> = yaml.load(file.inputStream)
        if (!mapping.containsKey(MAPPING_KEY)) {
            return false
        }

        return mapping[MAPPING_KEY].toString()
            .startsWith("v2")
    }

    companion object {
        const val MAPPING_KEY = "openapi-processor-mapping"
    }

}
