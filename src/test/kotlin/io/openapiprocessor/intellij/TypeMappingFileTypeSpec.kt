/*
 * Copyright 2026 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.FakeVirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.RunMethodInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@RunInEdt
@TestApplication
class TypeMappingFileTypeSpec {

    private val projectFixture = projectFixture()
    private val moduleFixture = projectFixture.moduleFixture()
    private val project get() = projectFixture.get()

    private fun detectFileType(file: VirtualFile): FileType {
        return FileTypeManager.getInstance().getFileTypeByFile(file)
    }

    @Test
    fun `ignores empty file`() {
        val emptyYaml = LightVirtualFile("empty.yaml", "")
        val detectedType = detectFileType(emptyYaml)
        assertNotEquals(TypeMappingFileType.NAME, detectedType.name)
        assertFalse(detectedType is TypeMappingFileType)
    }

    @Test
    fun `ignores other yaml file`() {
        val otherYaml = LightVirtualFile("other.yaml", "something: some value")
        val detectedType = detectFileType(otherYaml)
        assertNotEquals(TypeMappingFileType.NAME, detectedType.name)
        assertFalse(detectedType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'yaml' extension`() {
        val yaml = LightVirtualFile("mapping.yaml", """
            openapi-processor-mapping: v0
            options:
              package-name: io.openapiprocessor
            """.trimIndent())

        val detectedType = detectFileType(yaml)

        assertEquals(TypeMappingFileType.NAME, detectedType.name)
        assertTrue(detectedType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'yml' extension`() {
        val yaml = LightVirtualFile("mapping.yml", """
            openapi-processor-mapping: v0
            options:
              package-name: io.openapiprocessor
            """.trimIndent())

        val detectedType = detectFileType(yaml)

        assertEquals(TypeMappingFileType.NAME, detectedType.name)
        assertTrue(detectedType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'openapi-processor-mapping' identifier`() {
        val yaml = LightVirtualFile("mapping.yaml", """
            openapi-processor-mapping: v0
            options:
              package-name: io.openapiprocessor
            """.trimIndent())

        val detectedType = detectFileType(yaml)

        assertEquals(TypeMappingFileType.NAME, detectedType.name)
        assertTrue(detectedType is TypeMappingFileType)
    }

    @Test
    fun `detects file type with 'openapi-processor-spring' identifier`() {
        val yaml = LightVirtualFile("mapping.yaml", """
            openapi-processor-spring: v0
            options:
              package-name: io.openapiprocessor
            """.trimIndent())

        val detectedType = detectFileType(yaml)

        assertEquals(TypeMappingFileType.NAME, detectedType.name)
        assertTrue(detectedType is TypeMappingFileType)
    }

    @Test
    @RunMethodInEdt
    fun `ignores directory`() {
        val projectBaseDir = project.guessProjectDir() ?: error("No project directory found")

        val directory: VirtualFile = runWriteAction {
            projectBaseDir.createChildDirectory(this, "directory")
        }

        val detectedType = detectFileType(directory)

        assertNotEquals(TypeMappingFileType.NAME, detectedType.name)
        assertFalse(detectedType is TypeMappingFileType)
    }

    @Test
    fun `ignores FakeVirtualFile`() {
        val fake = FakeVirtualFile(project.guessProjectDir()!!, "fake")
        assertFalse(TypeMappingFileType().isMyFileType(fake))
    }

    private val jarFixture = testFixture {
        val tmpDir = directoryContent {
            zip("yaml.jar") {
                dir("resources") {
                    file("a.yaml", "openapi-processor-mapping: v0\n")
                }
            }
        }.generateInVirtualTempDir()

        val jar = tmpDir.findChild("yaml.jar")
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(jar!!)

        initialized(jarRoot) {
            edtWriteAction {
                tmpDir.delete(tmpDir)
            }
        }
    }

    @Test
    fun `detects yaml in jar`() {
        val jar = jarFixture.get()!!
        val yamlInJar = jar.findFileByRelativePath("resources/a.yaml")!!

        val detectedType = detectFileType(yamlInJar)

        assertEquals(TypeMappingFileType.NAME, detectedType.name)
        assertTrue(detectedType is TypeMappingFileType)
    }
}
