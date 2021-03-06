/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.GlobalSearchScope
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

    fun getMethod(name: String): PsiMethod {
        return JavaMethodNameIndex.getInstance()
            .get(name, project, GlobalSearchScope.allScope(project))
            .first()
    }

}
