/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class TypeMappingFileType : LanguageFileType(YAMLLanguage.INSTANCE, true) {

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

}
