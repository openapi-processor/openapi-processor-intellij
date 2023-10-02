/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.listener

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class LightCodeInsightListener : TestListener {
    var fixture: CodeInsightTestFixture? = null

    override suspend fun beforeTest(testCase: TestCase) {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder = factory.createLightFixtureBuilder()
        val temp = LightTempDirTestFixtureImpl(true)
        fixture = factory.createCodeInsightFixture(builder.fixture, temp)
        fixture?.setUp()
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        fixture?.tearDown()
    }
}
