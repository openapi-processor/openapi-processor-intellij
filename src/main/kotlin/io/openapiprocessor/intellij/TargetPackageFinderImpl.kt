/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
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
 * First it needs to find the module with the production source code. The mapping file is usually
 * not in the same module but in the "parent" module.
 * Second it needs to find the target package in a source root in the compiler output path of the
 * production source code module. For gradle typically in the *build* folder and for maven in the
 * *target* folder.
 */
class TargetPackageFinderImpl : TargetPackageFinder {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun findPackageDir(pkgName: String, mappingModule: Module): PsiDirectory? {
        log.debug("looking for generated pkg '{}' of mapping.yaml in module '{}'", pkgName, mappingModule.name)

        val pkg = getPackage(mappingModule, pkgName)
        val modules = findCandidateModules(mappingModule)

        modules.forEach {
            val out = getOutputPath(it)
                ?: return null

            val srcRoots = getSourceRoots(it)
            val prefixes = findSourceRootsInOutputPath(srcRoots, out)
            if (prefixes.isEmpty())
                return null

            val prefix = prefixes.maxBy { p -> p.length }!!
            val target = findPkgWithPrefix(pkg, prefix)
            return target
        }

        return null
    }

    private fun findPkgWithPrefix(pkg: PsiPackage?, prefix: String): PsiDirectory? {
        val target: PsiDirectory? = pkg?.directories?.first {
            it.virtualFile.path.startsWith(prefix)
        }

        if (target == null) {
            log.debug("did not find target pkg location!")
            return null
        }

        log.debug("found target pkg location: {}", target.virtualFile.path)
        return target
    }

    private fun findSourceRootsInOutputPath(sourceRoots: Array<VirtualFile>, out: String): List<String> {
        val prefixes = mutableListOf<String>()
        sourceRoots.forEach { r ->
            val prefix = out.commonPrefixWith(r.path)
            if (prefix.isNotEmpty()) {
                prefixes.add(prefix)
            }
        }

        log.debug("source roots in module output path ({}):", prefixes.size)
        prefixes.forEach {
            log.debug(">> {}", it)
        }
        return prefixes
    }

    private fun getSourceRoots(module: Module): Array<VirtualFile> {
        val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots

        log.debug("module source roots ({}):", sourceRoots.size)
        sourceRoots.forEach {
            log.debug(">> {}", it.path)
        }
        return sourceRoots
    }

    private fun getOutputPath(module: Module): String? {
        val out = CompilerPaths.getModuleOutputPath(module, false)
        if (out == null) {
            log.warn("module output path: <unavailable>")
        }
        return out
    }

    private fun findCandidateModules(module: Module): List<Module> {
        log.debug("looking for nested modules in module '{}' with output path", module.name)

        val parentRoot = findContentRoot(module) ?: return emptyList()

        return ModuleManager.getInstance(/*project*/module.project).modules
            .filter { it.name != module.name }
            .filter { isNestedModule(it, parentRoot) }
            .filter { hasOutputPath(it) }
    }

    private fun hasOutputPath(module: Module): Boolean {
        val outputPath = CompilerPaths.getModuleOutputPath(module, false)

        if (outputPath == null) {
            log.debug(">> no output path in module '{}'", module.name)
        } else {
            log.debug(">> found output path in module '{}': '{}'", module.name, outputPath)
        }

        return outputPath != null
    }

    private fun isNestedModule(module: Module, parentRoot: String): Boolean {
        val nested: Boolean

        val candidateRoots = ModuleRootManager.getInstance(module).sourceRoots
        nested = candidateRoots.any {
            it.path.startsWith(parentRoot)
        }

        if (nested) {
            log.debug(">> nested module '{}'", module.name)
        } else {
            log.debug(">> not nested module '{}'", module.name)
        }

        return nested
    }

    private fun findContentRoot(module: Module): String? {
        val roots = ModuleRootManager.getInstance(module).contentRoots
            .map {
                it.path
            }

        if (roots.isEmpty()) {
            log.warn("found no content root in module '{}'", module.name)
            return null
        }

        if (roots.size > 1) {
            log.warn("found multiple content roots in module '{}'", module.name)
            roots.forEach {
                log.warn(">> {}", it)
            }
            return null
        }

        log.debug("found content root in module '{}': '{}'", module.name, roots.first())
        return roots.first()
    }

    private fun getPackage(module: Module, pkgName: String): PsiPackage? {
        val pkg = JavaPsiFacade.getInstance(/*project*/module.project).findPackage(pkgName)

        log.debug("found package location candidates ({}):", pkg?.directories?.size)
        pkg?.directories?.forEach {
            log.debug(">> {}", it.virtualFile.path)
        }
        return pkg
    }

}
