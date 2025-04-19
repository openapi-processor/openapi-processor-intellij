/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * find the package directories of the `package-name` configuration in the `mapping.yaml`.
 */
class TargetPackageServiceImpl : TargetPackageService {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun findPackageDirs(pkgName: String, mappingModule: Module): List<PsiDirectory> {
        log.debug(
            "looking for generated pkg '{}' of mapping.yaml in module '{}' of project '{}'",
            pkgName,
            mappingModule.name,
            mappingModule.project.name
        )

        val pkg = JavaPsiFacade.getInstance(mappingModule.project).findPackage(pkgName)

        log.debug("found package locations ({}):", pkg?.directories?.size)
        pkg?.directories?.forEach {
            log.debug(">> {}", it.virtualFile.path)
        }

        return pkg?.directories?.toList() ?: return emptyList()
    }
}
