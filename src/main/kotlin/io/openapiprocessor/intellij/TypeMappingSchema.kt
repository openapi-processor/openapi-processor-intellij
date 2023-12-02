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
        return mutableListOf(
            TypeMappingSchemaProvider("v5"),
            TypeMappingSchemaProvider("v4"),
            TypeMappingSchemaProvider("v3"),
            TypeMappingSchemaProvider("v2.1"),
            TypeMappingSchemaProvider("v2")
        )
    }

    class TypeMappingSchemaProvider(private val version: String) : JsonSchemaFileProvider {
        private val schema = getSchema()

        override fun isAvailable(file: VirtualFile): Boolean {
            return file.fileType.name == name
        }

        override fun getName(): String {
            return "${TypeMappingFileType.NAME} $version"
        }

        override fun getSchemaFile(): VirtualFile? {
            return schema
        }

        override fun getSchemaType(): SchemaType {
            return SchemaType.remoteSchema
        }

        private fun getSchema(): VirtualFile? {
            // same as https://openapiprocessor.io/schemas/mapping/mapping-$version.json
            return HttpsFileSystem.getHttpsInstance().findFileByPath(
                "raw.githubusercontent.com" +
                    "/openapi-processor/openapi-processor" +
                    "/master/public/schemas/mapping/mapping-$version.json"
            )
        }
    }

    companion object {
       // const val SCHEMA_NAME = "openapi-processor mapping"
    }
}
