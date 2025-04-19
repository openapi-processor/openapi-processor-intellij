/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiDirectory

interface TargetPackageService {
    fun findPackageDirs(pkgName: String, mappingModule: Module): List<PsiDirectory>
}
