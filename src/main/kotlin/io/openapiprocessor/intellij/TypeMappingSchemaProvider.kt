/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.http.HttpsFileSystem
import com.intellij.psi.PsiFile
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider

class TypeMappingSchemaProvider: ContentAwareJsonSchemaFileProvider {

    override fun getSchemaFile(file: PsiFile): VirtualFile? {
        if (!isMappingFile(file))
            return null

        val start = String(file.virtualFile.inputStream.readNBytes(64))
        val regex = Regex("""^${TypeMappingFileType.PREFIX}-([a-z]+):\s+(v\d+)\s*$""", RegexOption.MULTILINE)
        val match = regex.find(start) ?: return null

        val type = match.groups[1] ?: return null
        val version = match.groups[2] ?: return null

        return getSchema(type.value, version.value)
    }

    private fun getSchema(type: String, version: String): VirtualFile? {
        // same as https://openapiprocessor.io/schemas/mapping/$type-$version.json
        @Suppress("UnstableApiUsage")
        return HttpsFileSystem.getHttpsInstance().findFileByPath(
            "raw.githubusercontent.com" +
                "/openapi-processor/openapi-processor" +
                "/master/public/schemas/mapping/$type-$version.json"
        )
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        return file.viewProvider.fileType is TypeMappingFileType
    }
}
