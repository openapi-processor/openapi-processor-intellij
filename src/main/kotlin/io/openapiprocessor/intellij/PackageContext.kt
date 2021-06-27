/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage

class PackageContext(val pkgName: String, val mapping: PsiFile) {
    var pkg: PsiPackage? = null
    var module: Module? = null

    fun println() {
        println("package line marker ->")
        printlnMapping()
        printlnModule()
        printlnPkgCandidates()
    }

    private fun printlnMapping() {
        println("| mapping path: ${mapping.virtualFile.path}")
    }

    private fun printlnModule() {
        println("| mapping module: ${module?.name}")
    }

    private fun printlnPkgCandidates() {
        val candidates = pkg?.directories
        candidates?.forEach {
            println("| candidate pkg dir: ${it.virtualFile.path}")
        }
    }

}
