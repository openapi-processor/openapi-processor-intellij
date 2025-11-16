/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VfsUtil.findFileByURL
import com.intellij.openapi.vfs.VfsUtilCore.convertToURL
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.openapiprocessor.intellij.support.LightInsightTestCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileTypeSpec : LightInsightTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/filetype"
    }

    @Test
    fun `detects file type with 'yaml' extension`() {
        assertEquals(TypeMappingFileType.NAME, openFile("mapping.yaml").fileType.name)
    }

    @Test
    fun `detects file type with 'yml' extension`() {
        assertEquals(TypeMappingFileType.NAME, openFile("mapping.yml").fileType.name)
    }

    @Test
    fun `ignores empty file`() {
        assertFalse(openFile("empty.yaml").fileType.name.startsWith(TypeMappingFileType.NAME))
    }

    @Test
    fun `ignores directory`() {
        assertFalse(createDir("foo").fileType.name.startsWith(TypeMappingFileType.NAME))
    }

    @Test
    fun `detects yaml in jar`() {
        val tmpDir = directoryContent {
            zip("yaml.jar") {
                dir("resources") {
                    file("a.yaml", "openapi-processor-mapping: v2\n")
                }
            }
        }.generateInVirtualTempDir()

        addJar(tmpDir.path, "yaml.jar")

        val url = convertToURL("jar://${tmpDir.path}/yaml.jar!/resources/a.yaml")!!
        val yml = findFileByURL(url)

        assertTrue(yml!!.fileType.name.startsWith(TypeMappingFileType.NAME))
    }
}
