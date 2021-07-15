/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.Icon

class TypeMappingLineMarker: RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

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

        val pkgName = keyValue.valueText
        val module = findModule(element.containingFile)
            ?: return

        val target = element.project
            .service<TargetPackageService>()
            .findPkgInTargetDir(pkgName, module)

        if (target != null) {
            val builder = NavigationGutterIconBuilder
                .create(getPackageIcon())
                .setTargets(target)
                .setTooltipText(PACKAGE_EXISTS_TOOLTIP_TEXT)

            result.add(builder.createLineMarkerInfo(element))
        } else {
            val builder = NavigationGutterIconBuilder
                .create(getPackageIcon())
                .setTarget(element)
                .setTooltipText(PACKAGE_MISSING_TOOLTIP_TEXT)

            result.add(builder.createLineMarkerInfo(element))
        }

        super.collectNavigationMarkers(element, result)
    }

    private fun findModule(file: PsiFile): Module? {
        val module = ModuleUtil.findModuleForFile(file)
        if (module == null) {
            log.warn("failed to find module of {}", file.virtualFile.path)
        }
        return module
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        // static method on TypeMappingFileType
        return file.viewProvider.fileType is TypeMappingFileType
    }

    private fun getPackageIcon(): Icon {
        return AllIcons.Modules.GeneratedFolder
    }

    companion object {
        const val PACKAGE_KEY = "package-name"
        const val PACKAGE_EXISTS_TOOLTIP_TEXT = "Navigate to generated package"
        const val PACKAGE_MISSING_TOOLTIP_TEXT = "Generated package does not yet exists"
    }

}
