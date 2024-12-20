/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer
import com.intellij.icons.AllIcons
import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.Iconable
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.util.TextWithIcon
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import javax.swing.Icon

/**
 * line marker for the package-name key in the mapping.yaml. It navigates to any folder with the same package name.
 */
class TypeMappingLineMarker : RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    class Renderer() : PsiTargetPresentationRenderer<PsiElement>() {

        override fun getContainerText(element: PsiElement): String? {
            return SymbolPresentationUtil.getSymbolContainerText(element)
        }

        override fun getElementText(element: PsiElement): String {
            return SymbolPresentationUtil.getSymbolPresentableText(element)
        }

        override fun getIcon(element: PsiElement): Icon? {
            return element.getIcon(Iconable.ICON_FLAG_VISIBILITY)
        }

        override fun getPresentation(element: PsiElement): TargetPresentation {
            if (element !is Directory) {
                return super.getPresentation(element)
            }

            val moduleLocation = getModuleLocation(element)
            val locationText = if (element.location == null) {
                moduleLocation?.text
            } else {
                "${element.location}     ${moduleLocation?.text}"
            }

            return TargetPresentation.builder(getElementText(element))
                .icon(getIcon(element))
                .containerText(getContainerText(element))
                .locationText(locationText)
                .presentation()
        }

        private fun getModuleLocation(element: PsiElement): TextWithIcon? {
            return ModuleRendererFactory.findInstance(element).getModuleTextWithIcon(element)
        }
    }

    class Directory(val location: String?, private val delegate: PsiDirectory) : PsiDirectory by delegate, PsiElement

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

        val pkgDirs = service<TargetPackageService>()
            .findPackageDirs(pkgName, module)

        val targets = addLocations(pkgDirs!!)

        val y = Supplier<PsiTargetPresentationRenderer<PsiElement>> { Renderer() }


        if (targets.isEmpty()) {
            val builder = NavigationGutterIconBuilder
                .create(getPackageIcon())
                .setTarget(element)
                .setTooltipText(PACKAGE_MISSING_TOOLTIP_TEXT)

            result.add(builder.createLineMarkerInfo(element))

        } else {
            val builder = NavigationGutterIconBuilder
                .create(getPackageIcon())
                .setTooltipTitle(PACKAGE_POPUP_TITLE)
                .setTooltipText(PACKAGE_EXISTS_TOOLTIP_TEXT)
                .setPopupTitle(PACKAGE_POPUP_TITLE)
                .setTargets(targets)
                .setTargetRenderer(y)

            result.add(builder.createLineMarkerInfo(element))
        }
    }

    private fun addLocations(pkgDirs: List<PsiDirectory>): List<Directory> {
        if (pkgDirs.size < 2) {
            return pkgDirs
                .map { Directory(null, it) }
        }

        val commonPrefix = pkgDirs
            .map { it.virtualFile.path }
            .zipWithNext()
            .map { it.first.commonPrefixWith(it.second) }
            .minBy { it.length }

        log.debug(">> commonPrefix: $commonPrefix")

        val commonSuffix = pkgDirs
            .map { it.virtualFile.path }
            .zipWithNext()
            .map { it.first.commonSuffixWith(it.second) }
            .minBy { it.length }

        log.debug(">> commonSuffix: $commonSuffix")

        return pkgDirs
            .map {
                Directory(it.virtualFile.path.drop(commonPrefix.length).dropLast(commonSuffix.length), it)
            }
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
        const val PACKAGE_POPUP_TITLE = "Package Locations"
        const val PACKAGE_EXISTS_TOOLTIP_TEXT = "Navigate to package"
        const val PACKAGE_MISSING_TOOLTIP_TEXT = "Package does not yet exists"
    }
}
