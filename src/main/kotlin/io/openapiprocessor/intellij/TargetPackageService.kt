/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiPackage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class TargetPackageService(private val project: Project) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * the **targetDir** option of the processor is *hidden* in the project build file. Try to
     * find it by assuming that the target package is a source/content root near the compiler
     * output path. For gradle typically in the *build* folder and for maven in the *target*
     * folder.
     */
    fun findPkgInTargetDir(pkgName: String, module: Module): PsiDirectory? {
        log.debug("looking for pkg '{}' in module '{}'", pkgName, module)

        val pkg = getPackage(pkgName)
        val out = getOutputPath(module)
            ?: return null

        val srcRoots = getSourceRoots(module)
        val prefixes = findSourceRootsInOutputPath(srcRoots, out)
        if (prefixes.isEmpty())
            return null

        val prefix = prefixes.maxByOrNull { it.length }!!
        val target = findPkgWithPrefix(pkg, prefix)
        return target
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
            return null
        }

        log.debug("module output path: {}", out)
        return out
    }

    private fun getPackage(pkgName: String): PsiPackage? {
        val pkg = JavaPsiFacade.getInstance(project).findPackage(pkgName)

        log.debug("package location candidates ({}):", pkg?.directories?.size)
        pkg?.directories?.forEach {
            log.debug(">> {}", it.virtualFile.path)
        }
        return pkg
    }

}
