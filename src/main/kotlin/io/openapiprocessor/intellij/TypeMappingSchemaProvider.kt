/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.http.HttpsFileSystem
import com.intellij.psi.PsiFile
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UnstableApiUsage")
class TypeMappingSchemaProvider: ContentAwareJsonSchemaFileProvider {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun getSchemaFile(file: PsiFile): VirtualFile? {
        if (!isMappingFile(file))
            return null

        val start = String(file.virtualFile.inputStream.readNBytes(64))
        val regex = Regex("""^${TypeMappingFileType.KEY}:\s+(v\d+)\s*$""", RegexOption.MULTILINE)
        val match = regex.find(start) ?: return null
        val version = match.groups[1] ?: return null

        return getSchema(version.value)
    }

    private fun getSchema(version: String): VirtualFile? {
        // same as https://openapiprocessor.io/schemas/mapping/mapping-$version.json
        return HttpsFileSystem.getHttpsInstance().findFileByPath(
            "raw.githubusercontent.com" +
                "/openapi-processor/openapi-processor" +
                "/master/public/schemas/mapping/mapping-$version.json"
        )
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        return file.viewProvider.fileType is TypeMappingFileType
    }
}
