/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiDirectory
import io.openapiprocessor.intellij.TargetPackageFinder

class TargetPackageFinderStub(private val pkg: PsiDirectory?): TargetPackageFinder {

    override fun findPackageDir(pkgName: String, mappingModule: Module): PsiDirectory? {
        return pkg
    }

}
