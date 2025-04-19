/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import io.openapiprocessor.intellij.ModuleService

@Service
class ModuleServiceStub: ModuleService {

    override fun findModules(element: PsiElement): List<Module> {
        val module = ProjectRootManager.getInstance(element.project)
            .fileIndex
            .getModuleForFile(element.containingFile.virtualFile)

        return listOfNotNull(module)
    }
}
