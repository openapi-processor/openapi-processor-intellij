/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.http.HttpsFileSystem
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

class TypeMappingSchema : JsonSchemaProviderFactory {

    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider> {
        return mutableListOf(TypeMappingSchemaProvider)
    }

    object TypeMappingSchemaProvider : JsonSchemaFileProvider {
        private val schema = HttpsFileSystem.getHttpsInstance().findFileByPath(SCHEMA_URL)

        override fun isAvailable(file: VirtualFile): Boolean {
            return file.fileType.name == TypeMappingFileType.NAME
        }

        override fun getName(): String {
            return SCHEMA_NAME
        }

        override fun getSchemaFile(): VirtualFile? {
            return schema
        }

        override fun getSchemaType(): SchemaType {
            return SchemaType.remoteSchema
        }

    }

    companion object {
        const val SCHEMA_NAME = "openapi-processor mapping"

        const val SCHEMA_URL = "raw.githubusercontent.com" +
            "/openapi-processor/openapi-processor-core" +
            "/master/src/main/resources/mapping/v2/mapping.yaml.json"
    }

}
