/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VfsUtil.findFileByURL
import com.intellij.openapi.vfs.VfsUtilCore.convertToURL
import com.intellij.openapi.vfs.newvfs.impl.FakeVirtualFile
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.junit5.fixture.virtualFileFixture
import io.openapiprocessor.intellij.support.virtualDirFixture
import io.openapiprocessor.intellij.support.virtualJarFixture
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


@TestApplication
class FileTypeSpec {
    val project = projectFixture()
    val module = project.moduleFixture("src")
    val sourceRoot = module.sourceRootFixture()

    val emptyYaml = sourceRoot.virtualFileFixture("empty.yaml", "")
    val otherYaml = sourceRoot.virtualFileFixture("other.yaml", "something: some value")

    val mappingYaml = sourceRoot.virtualFileFixture(
        "mapping.yaml", """
        openapi-processor-mapping: v0
        options:
          package-name: io.openapiprocessor
    """.trimIndent()
    )

    val mappingYml = sourceRoot.virtualFileFixture(
        "mapping.yml", """
        openapi-processor-mapping: v0
        options:
          package-name: io.openapiprocessor
    """.trimIndent()
    )

    val directory = sourceRoot.virtualDirFixture("folder")

    val jar = sourceRoot.virtualJarFixture(module, "yaml.jar")


    @Test
    fun `ignores empty file`() {
        assertNotEquals(TypeMappingFileType.NAME, emptyYaml.get().fileType.name)
        assertFalse(emptyYaml.get().fileType is TypeMappingFileType)
    }

    @Test
    fun `ignores other yaml file`() {
        assertNotEquals(TypeMappingFileType.NAME, otherYaml.get().fileType.name)
        assertFalse(otherYaml.get().fileType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'yaml' extension`() {
        assertEquals(TypeMappingFileType.NAME, mappingYaml.get().fileType.name)
        assertTrue(mappingYaml.get().fileType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'yml' extension`() {
        assertEquals(TypeMappingFileType.NAME, mappingYml.get().fileType.name)
        assertTrue(mappingYml.get().fileType is TypeMappingFileType)
    }

    @Test
    fun `ignores directory`() {
        assertNotEquals(TypeMappingFileType.NAME, directory.get().fileType.name)
        assertFalse(otherYaml.get().fileType is TypeMappingFileType)
    }

    @Test
    fun `ignores FakeVirtualFile`() {
        assertFalse(TypeMappingFileType().isMyFileType(FakeVirtualFile(sourceRoot.get().virtualFile, "fake")))
    }

    @Test  // probably useless
    fun `detects yaml in jar`() {
        val url = convertToURL("jar://" + jar.get().path + "/yaml.jar!/resources/a.yaml")
        val yml = findFileByURL(url!!)!!
        assertEquals(TypeMappingFileType.NAME, yml.fileType.name)
    }
}
