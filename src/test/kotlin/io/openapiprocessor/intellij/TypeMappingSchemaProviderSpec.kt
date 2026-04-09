/*
 * Copyright 2026 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.application.runReadAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.junit5.fixture.virtualFileFixture
import com.intellij.testFramework.utils.vfs.getPsiFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class TypeMappingSchemaProviderSpec {
    companion object {
        val project = projectFixture()
        val module = project.moduleFixture("main")
        val sourceRoot = module.sourceRootFixture()

        val mappingYaml = sourceRoot.virtualFileFixture(
            "mapping.yaml", """
            openapi-processor-mapping: v18
            options:
              package-name: io.openapiprocessor
            """.trimIndent())

        val springMappingYaml = sourceRoot.virtualFileFixture(
            "spring-mapping.yaml", """
            openapi-processor-spring: v1
            options:
              package-name: io.openapiprocessor
            """.trimIndent())
    }

    @Test
    fun `detects openapi-processor-mapping schema`() {
        val provider = TypeMappingSchemaProvider()

        runReadAction {
            val schema = provider.getSchemaFile(mappingYaml.get().getPsiFile(project.get()))

            assertEquals(
                "raw.githubusercontent.com" +
                "/openapi-processor/openapi-processor" +
                "/master/public/schemas/mapping/mapping-v18.json",
                schema!!.canonicalPath)
        }
    }

    @Test
    fun `detects openapi-processor-spring schema`() {
        val provider = TypeMappingSchemaProvider()

        runReadAction {
            val schema = provider.getSchemaFile(springMappingYaml.get().getPsiFile(project.get()))

            assertEquals(
                "raw.githubusercontent.com" +
                "/openapi-processor/openapi-processor" +
                "/master/public/schemas/mapping/spring-v1.json",
                schema!!.canonicalPath)
        }
    }
}
