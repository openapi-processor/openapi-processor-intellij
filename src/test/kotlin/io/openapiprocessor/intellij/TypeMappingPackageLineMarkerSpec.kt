/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.openapiprocessor.intellij.listener.LightCodeInsightListener
import io.openapiprocessor.intellij.support.TargetPackageServiceStub
import io.openapiprocessor.intellij.support.getTargets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TypeMappingPackageLineMarkerSpec: StringSpec({
    val test = extension(LightCodeInsightListener("src/test/testdata/package-name"))
    val expectedPkgDir = "io/openapiprocessor"

    beforeTest {
        val pkgDir = test.createDir(expectedPkgDir)
        val psiDir = readAction { test.findPsiDir(pkgDir) }
        val stub = TargetPackageServiceStub(psiDir)
        test.replaceService(TargetPackageService::class.java, stub)
    }

    "adds navigation gutter at package-name" {
        withContext(Dispatchers.EDT) {
            val gutters = test.findAllGutters("api/mapping-package-name.yaml")
            val gutter = gutters.first()

            gutter.icon shouldBe AllIcons.Modules.GeneratedFolder
            gutter.tooltipText shouldBe TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT

            getTargets(gutter).first() shouldEndWith expectedPkgDir
        }
    }

    "adds navigation gutter at package-names.base & package-names.location" {
        withContext(Dispatchers.EDT) {
            val gutters = test.findAllGutters("api/mapping-package-names.yaml")
            gutters.size shouldBe 2

            val gutter0 = gutters[0]
            gutter0.icon shouldBe AllIcons.Modules.GeneratedFolder
            gutter0.tooltipText shouldBe TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT
            getTargets(gutter0).first() shouldEndWith expectedPkgDir

            val gutter1 = gutters[1]
            gutter1.icon shouldBe AllIcons.Modules.GeneratedFolder
            gutter1.tooltipText shouldBe TypeMappingPackageLineMarker.I18n.TOOLTIP_TEXT
            getTargets(gutter1).first() shouldEndWith expectedPkgDir
        }
    }
})
