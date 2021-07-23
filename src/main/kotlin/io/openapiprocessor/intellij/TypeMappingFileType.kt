/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.PlainTextLikeFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.FakeVirtualFile
import org.jetbrains.yaml.YAMLLanguage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.swing.Icon

open class TypeMappingFileType :
    LanguageFileType(YAMLLanguage.INSTANCE, true),
    FileTypeIdentifiableByVirtualFile, PlainTextLikeFileType {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

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
        println("isMyFilType: ${file.name}")

        // when creating a new yaml file by pressing return in the new file dialog the file is
        // a FakeVirtualFile. Is there a better way to handle it?
        if (file is FakeVirtualFile)
            return false

        if (file.isDirectory)
            return false

        if (!extensions.contains(file.extension?.toLowerCase()))
            return false

        if (file.length == 0L)
            return false

        return checkMappingKey(file)
    }

    private fun checkMappingKey(file: VirtualFile): Boolean {
        return try {
            MAPPING_REGEX.containsMatchIn(String(file.inputStream.readNBytes(64)))
        } catch(e: IOException) {
            log.error("breaking file {}", file.name, e)
            false
        }
    }

    private val extensions = defaultExtension.split(";")

    companion object {
        const val NAME = "openapi-processor mapping"
        const val MAPPING_KEY = "openapi-processor-mapping"

        val MAPPING_REGEX = Regex("""^${MAPPING_KEY}:\s+v2""")

        val INSTANCE = TypeMappingFileType()
    }

}
