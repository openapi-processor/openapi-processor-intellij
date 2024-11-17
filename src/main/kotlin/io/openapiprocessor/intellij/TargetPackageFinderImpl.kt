/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.modules
import com.intellij.openapi.project.rootManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiPackage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * find the generated package directory of the `package-name` configuration in the `mapping.yaml`.
 *
 * since the **targetDir** option of the processor is *hidden* in the project build file (gradle
 * or maven) it takes a few steps to find it.
 *
 * 1. lookup the package directories
 * 2. find the package directories in the module of the mapping.yaml
 * 3. find the child modules in the module of the mapping.yaml
 * 4. find the best matching package directory from the source roots (i.e. the longest common prefix)
 *
 * The resulting directory should be the generated folder in the gradle *build* folder or in the maven
 * *target* folder.
 */
class TargetPackageFinderImpl : TargetPackageFinder {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun findPackageDir(pkgName: String, mappingModule: Module): PsiDirectory? {
        log.debug("looking for generated pkg '{}' of mapping.yaml in module '{}' of project '{}'",
            pkgName,
            mappingModule.name,
            mappingModule.project.name)

        val modulePath = getModulePath(mappingModule) ?: return null
        val targetPkg = getTargetPackage(mappingModule, pkgName)
        val pkgDirs = filterPackageDirs(targetPkg, modulePath)
        val childModules = filterChildModules(mappingModule, modulePath)

        val targetDir = findTargetPackage(childModules, pkgDirs)
        if (targetDir != null) {
            log.debug("found matching targetDir pkg {}", targetDir)
        } else {
            log.debug("found no matching targetDir pkg")
        }

        return targetDir
    }

    private fun getTargetPackage(module: Module, pkgName: String): PsiPackage? {
        val pkg = JavaPsiFacade.getInstance(module.project).findPackage(pkgName)

        log.debug("found package locations ({}):", pkg?.directories?.size)
        pkg?.directories?.forEach {
            log.debug(">> {}", it.virtualFile.path)
        }
        return pkg
    }

    private fun getModulePath(module: Module): String? {
        val path = module.guessModuleDir()?.path
        log.debug("found module path: '{}'", path)
        return path
    }

    private fun filterPackageDirs(pkg: PsiPackage?, modulePath: String): List<PsiDirectory> {
        val dirs = pkg
            ?.directories
            ?.filter { it.virtualFile.path.startsWith(modulePath) }
            .orEmpty()

        log.debug("found packages in module: '{}'", modulePath)
        dirs.forEach {
            log.debug(">> '{}'", it.virtualFile.path)
        }

        return dirs
    }

    private fun filterChildModules(module: Module, modulePath: String): List<Module> {
        val matches = module.project.modules
            .filter { it.guessModuleDir()?.path?.startsWith(modulePath) ?: false }

        log.debug("found nested modules of module: '{}'", modulePath)
        matches.forEach {
            log.debug(">> '{}' '{}'", it.name, it.guessModuleDir())
        }

        return matches
    }

    private fun findTargetPackage(modules: List<Module>, pkgDirs: List<PsiDirectory>): PsiDirectory? {
        var commonPrefix = ""
        var matchingPkg: PsiDirectory? = null

        modules.forEach { module ->
            log.debug("module '{}' source roots", module.name)

            module.rootManager.sourceRoots.forEach { root ->
                log.debug(">> source root: {}", root.path)

                pkgDirs.forEach {
                    val dir = it.virtualFile.path

                    if (dir.startsWith(root.path)) {
                        val common = dir.commonPrefixWith(root.path)
                        if (common.length > commonPrefix.length) {
                            commonPrefix = common
                            matchingPkg = it
                        }
                    }
                }
            }
        }

        return matchingPkg
    }
}
