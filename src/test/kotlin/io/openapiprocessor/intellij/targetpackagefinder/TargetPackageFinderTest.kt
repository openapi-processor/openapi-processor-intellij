/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.targetpackagefinder

import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtil
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.openapiprocessor.intellij.TargetPackageService
import io.openapiprocessor.intellij.support.HeavyBaseTestCase

class TargetPackageFinderTest: HeavyBaseTestCase() {

    override fun setUpModule() {
        super.setUpModule()

        when {
            annotatedWith(SimpleModules::class.java) -> {
                SimpleModulesFactory(this).setup()
            }
        }
    }

    @SimpleModules
    fun `test finds target package directories`() {
        val mapping = getBaseRelativePsiFile("src/api/mapping.yaml")
        val module = ModuleUtil.findModuleForFile(mapping)!!

        // when
        val targets = service<TargetPackageService>()
            .findPackageDirs("io.openapiprocessor", module)

        // then
        targets.shouldHaveSize(3)

        val expectedMain = getBaseRelativePsiDir("src/main/io/openapiprocessor")
        val expectedTest = getBaseRelativePsiDir("src/test/io/openapiprocessor")
        val expectedApi = getBaseRelativePsiDir("build/openapi/io/openapiprocessor")

        targets.shouldContainAll(expectedMain, expectedTest, expectedApi)
    }
}
