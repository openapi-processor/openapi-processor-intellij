/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


@TestApplication
class FileTypeSpec : LightJavaCodeInsightFixtureTestCase5() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/filetype"
    }

    private fun loadFile(path: String): VirtualFile {
        return fixture.configureByFile(path).virtualFile
    }

    @Test
    fun `detects file type with 'yaml' extension`() {
        val mapping = loadFile("mapping.yaml")
        assertEquals(TypeMappingFileType.NAME, mapping.fileType.name)
    }
}
