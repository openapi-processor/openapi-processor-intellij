/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.targetpackagefinder

import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtil
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
    fun `test finds target package directory`() {
        val mapping = getBaseRelativePsiFile("src/api/mapping.yaml")
        val module = ModuleUtil.findModuleForFile(mapping)!!

        // when
        val targets = project
            .service<TargetPackageService>()
            .findPackageDirs("io.openapiprocessor", module)

        // then
        val target = targets.first()
        val expected = getBaseRelativePsiDir("build/openapi/io/openapiprocessor")
        assertEquals(expected.virtualFile.path, target.virtualFile.path)
    }

}
