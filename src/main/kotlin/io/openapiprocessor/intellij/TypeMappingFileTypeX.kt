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
import javax.swing.Icon

class TypeMappingFileTypeX : LanguageFileType(YAMLLanguage.INSTANCE, true),
    FileTypeIdentifiableByVirtualFile {

    override fun getName(): String {
        return "openapi-processor mapping"
    }

    override fun getDescription(): String {
        return "OpenAPI-Processor Configuration"
    }

    override fun getDefaultExtension(): String {
        return ""
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/openapi-processor-p.svg")
    }

    override fun isMyFileType(file: VirtualFile): Boolean {
        if(!listOf("yaml", "yml").contains(file.extension)) {
            return false
        }

        if (!file.isInLocalFileSystem) {
            return false
        }

        val content = String(file.inputStream.readAllBytes())
        return content.lines().any {
            return it.matches(Regex("""^openapi-processor-mapping:\s*v2"""))
        }
    }

}
