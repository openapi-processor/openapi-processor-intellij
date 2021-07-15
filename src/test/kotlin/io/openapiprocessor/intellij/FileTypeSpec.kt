/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VfsUtil.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.openapiprocessor.intellij.listener.CodeInsightListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileTypeSpec : StringSpec({
    val test = CodeInsightListener()
    listener(test)

    lateinit var fixture: CodeInsightTestFixture

    beforeTest {
        fixture = test.fixture
        fixture.testDataPath = "src/test/testdata/filetype"
    }

    fun loadFile(path: String): VirtualFile {
        return fixture.configureByFile(path).virtualFile
    }

    fun createDir(path: String): VirtualFile {
        return fixture.tempDirFixture.findOrCreateDir(path)
    }

    "detects file type with 'yaml' extension" {
        val mapping = loadFile("src/api/mapping.yaml")
        mapping.fileType.name shouldBe TypeMappingFileType.NAME
    }

    "detects file type with 'yml' extension" {
        val mapping = loadFile("src/api/mapping.yml")
        mapping.fileType.name shouldBe TypeMappingFileType.NAME
    }

    "ignores empty file" {
        val mapping = loadFile("src/api/empty.yaml")
        mapping.fileType.name shouldNotBe TypeMappingFileType.NAME
    }

    "ignores directory" {
        val directory = withContext(Dispatchers.IO) { createDir("foo") }
        directory.fileType.name shouldNotBe TypeMappingFileType.NAME
    }

    "detects yaml in jar" {
        val tmpDir = directoryContent {
            zip("yaml.jar") {
                dir("resources") {
                    file("a.yaml", "openapi-processor-mapping: v2")
                }
            }
        }.generateInVirtualTempDir()

        PsiTestUtil.addLibrary(
            fixture.module,
            "yaml in jar",
            getUrlForLibraryRoot(File("${tmpDir.path}/yaml.jar"))
        )

        val url = convertToURL("jar://${tmpDir.path}/yaml.jar!/resources/a.yaml")!!
        val yml = findFileByURL(url)

        yml!!.fileType.name shouldBe TypeMappingFileType.NAME
    }

})
