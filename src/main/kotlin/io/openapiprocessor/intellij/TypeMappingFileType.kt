/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.yaml.YAMLLanguage
import org.yaml.snakeyaml.Yaml
import javax.swing.Icon

open class TypeMappingFileType :
    LanguageFileType(YAMLLanguage.INSTANCE, true),
    FileTypeIdentifiableByVirtualFile {

    override fun getName(): String {
        return NAME
    }

    override fun getDescription(): String {
        return "OpenAPI-Processor Configuration"
    }

    override fun getDefaultExtension(): String {
        return "yaml;yml"
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/openapi-processor-p.svg")
    }

    override fun isMyFileType(file: VirtualFile): Boolean {
        if (file.isDirectory) {
            return false
        }

        if (!extensions.contains(file.extension?.toLowerCase())) {
            return false
        }

        if (file.length == 0L) {
            return false
        }

        val yaml = Yaml()
        val mapping: Map<String, Any> = yaml.load(file.inputStream)
        if (!mapping.containsKey(MAPPING_KEY)) {
            return false
        }

        return mapping[TypeMappingFileType.MAPPING_KEY].toString()
            .startsWith("v2")
    }

    private val extensions = defaultExtension.split(";")

    companion object {
        const val NAME = "openapi-processor mapping"
        const val MAPPING_KEY = "openapi-processor-mapping"
    }

}
