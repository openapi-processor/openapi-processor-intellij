/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.http.HttpsFileSystem
import com.intellij.psi.PsiFile
import com.intellij.util.net.HttpConnectionUtils
import com.jetbrains.jsonSchema.extension.ContentAwareJsonSchemaFileProvider
import java.net.HttpURLConnection

const val GITHUB_PREFIX = "raw.githubusercontent.com/openapi-processor/openapi-processor/master/"

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

        // first, check if there is a "merged" version without external $refs
        val mergedUrl = "${GITHUB_PREFIX}public/schemas/mapping/$type-$version.merged.json"
        if(exists(mergedUrl)) {
            return HttpsFileSystem.getHttpsInstance().findFileByPath(mergedUrl)
        }

        // if there is no merged version try the original json schema
        return HttpsFileSystem.getHttpsInstance().findFileByPath(
            "${GITHUB_PREFIX}public/schemas/mapping/$type-$version.json")
    }

    private fun exists(url: String): Boolean {
        try {
            val connection = HttpConnectionUtils.openHttpConnection("https://${url}")
            connection.setRequestMethod("HEAD")
            return connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (_: Exception) {
            return false
        }
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        return file.viewProvider.fileType is TypeMappingFileType
    }
}
