/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage

@Service
class PackageService(private val project: Project) {

    fun foo() {
        println("foo: " + project.name)
    }

    fun findGeneratedPackage(pkgName: String, mapping: PsiFile): PsiDirectory? {
        val ctx = PackageContext(pkgName, mapping)
        ctx.pkg = getPackage(pkgName)
        ctx.module = getModule(mapping)
        return null
    }

    private fun getModule(mapping: PsiFile): Module? {
        return ModuleUtil.findModuleForFile(mapping.containingFile)
    }

    private fun getPackage(pkgName: String): PsiPackage? {
        val psi = JavaPsiFacade.getInstance(project)
        return psi.findPackage(pkgName)
    }

}
