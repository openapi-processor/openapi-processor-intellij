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
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.YAMLLanguage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.swing.Icon

abstract class TypeMappingFileType :
    LanguageFileType(YAMLLanguage.INSTANCE, true),
    FileTypeIdentifiableByVirtualFile, PlainTextLikeFileType {

    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun getName(): String {
        return NAME
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun getDescription(): String {
        return "OpenAPI-Processor configuration"
    }

    override fun getDefaultExtension(): String {
        return "yaml;yml"
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/mapping.svg", javaClass)
    }

    override fun isMyFileType(file: VirtualFile): Boolean {
        // when creating a new yaml file by pressing "return" in the new file dialog the file is
        // a FakeVirtualFile. Is there a better way to handle it?
        if (file is FakeVirtualFile)
            return false

        if (file.isDirectory)
            return false

        if (!extensions.contains(file.extension?.lowercase()))
            return false

        if (file.length == 0L)
            return false

        return checkMappingKey(file)
    }

    abstract fun getVersion(): String

    private fun checkMappingKey(file: VirtualFile): Boolean {
        return try {
            val start = String(file.inputStream.readNBytes(64))
            val regex = Regex("""^${KEY}:\s+v\d+\s+""")
            return regex.containsMatchIn(start)
        } catch (e: IOException) {
            log.info("failed to check file {} ({})", file.name, e.message)
            false
        }
    }

    private val extensions = defaultExtension.split(";")

    companion object {
        const val NAME = "openapi-processor mapping"
        const val KEY = "openapi-processor-mapping"

        fun isMappingFile(file: PsiFile): Boolean {
            return file.viewProvider.fileType is TypeMappingFileType
        }
    }
}
