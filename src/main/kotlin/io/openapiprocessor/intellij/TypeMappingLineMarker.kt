/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import javax.swing.Icon

class TypeMappingLineMarker: RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

        if (element.text != PACKAGE_KEY)
            return

        val keyValue = element.parent
        if (keyValue !is YAMLKeyValue)
            return

        val psi = JavaPsiFacade.getInstance(element.project)
        val pkg = psi.findPackage(keyValue.valueText)

        val mappingModule = ModuleUtil.findModuleForFile(element.containingFile)
        println("mapping module: ${mappingModule?.name}")

        val out = CompilerPaths.getModuleOutputPath(mappingModule, false)
        println("mapping module output path: ${out}")

        val sourceRoots = ModuleRootManager.getInstance(mappingModule!!).sourceRoots
        println("mapping module source roots: ${sourceRoots}")

        val prefixes = mutableSetOf<String>()
        sourceRoots.forEach { r ->
            val prefix = out?.commonPrefixWith(r.path)
            if (prefix != null && prefix.isNotEmpty()) {
                prefixes.add(prefix)
            }
        }

        var longestPrefix: String = ""
        prefixes.forEach {
            println("common prefix: $it")
            if (it.length > longestPrefix.length) {
                longestPrefix = it
            }
        }
        println("longest common prefix: $longestPrefix")

        var targetPkg: PsiDirectory? = null
        pkg?.directories?.forEach {
            if (it.virtualFile.path.startsWith(longestPrefix)) {
                targetPkg = it
                println("found target: $it")
            }
        }

        // if not match, not yt generated tooltip without target

        val builder = NavigationGutterIconBuilder
            .create(getPackageIcon())
            .setTargets(targetPkg)
            .setTooltipText(PACKAGE_TOOLTIP_TEXT)

        result.add(builder.createLineMarkerInfo(element))

        super.collectNavigationMarkers(element, result)
    }

    private fun getPackageIcon(): Icon {
        return AllIcons.Modules.GeneratedFolder
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        // static method on TypeMappingFileType
        return file.viewProvider.fileType is TypeMappingFileType
    }

    companion object {
        const val PACKAGE_KEY = "package-name"
        const val PACKAGE_TOOLTIP_TEXT = "Navigate to generated package"
    }

}
