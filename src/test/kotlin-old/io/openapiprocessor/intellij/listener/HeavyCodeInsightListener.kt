/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.listener

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult

class HeavyCodeInsightListener : TestListener {
    private var fixture: CodeInsightTestFixture? = null

    override suspend fun beforeAny(testCase: TestCase) {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder = factory.createFixtureBuilder(testCase.name.name)
        val tmpDir = factory.createTempDirTestFixture()

        fixture = factory.createCodeInsightFixture(builder.fixture, tmpDir)
        fixture?.setUp()
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        fixture?.tearDown()
    }
}
