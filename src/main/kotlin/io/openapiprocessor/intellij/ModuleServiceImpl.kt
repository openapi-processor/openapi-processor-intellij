/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class ModuleServiceImpl: ModuleService {
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun findModules(element: PsiElement): List<Module> {
        val finder = ModuleFinder(element.project)
        val modules = finder.findModules(element.containingFile.virtualFile.presentableUrl)

        if (modules.isNotEmpty()) {
            modules.forEach {
                log.debug("related modules of file '{}'", element.containingFile.name)
                log.debug("found module '{}'", it.name)
            }
        } else {
            log.debug("could not find module of file '{}'", element.containingFile.name)
        }

        return modules
    }
}
