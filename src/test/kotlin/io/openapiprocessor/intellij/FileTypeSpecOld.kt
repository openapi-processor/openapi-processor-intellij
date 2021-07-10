/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FileTypeSpecOld : StringSpec({
    var fixture: CodeInsightTestFixture? = null

    beforeTest {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder = factory.createLightFixtureBuilder()
        fixture = factory.createCodeInsightFixture(builder.fixture)
        fixture?.testDataPath = "src/test/testData"
        fixture?.setUp()
    }

    afterTest {
        fixture?.tearDown()
    }

    "detects file type" {
        val mapping = fixture?.configureByFile("src/api/mapping.yaml")
        mapping?.virtualFile?.fileType?.name shouldBe(TypeMappingFileType.NAME)
    }

})
