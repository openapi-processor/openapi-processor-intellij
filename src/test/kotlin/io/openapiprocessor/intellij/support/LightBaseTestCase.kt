/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

/**
 * improve/extend api of [BasePlatformTestCase]
 */
abstract class LightBaseTestCase : BasePlatformTestCase() {

    val fixture: CodeInsightTestFixture
        get() {
            return myFixture
        }

}
