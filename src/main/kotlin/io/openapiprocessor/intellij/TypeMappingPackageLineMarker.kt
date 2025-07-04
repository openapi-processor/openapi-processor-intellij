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
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.Key
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolContainerText
import com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolPresentableText
import com.intellij.util.TextWithIcon
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.Icon as JIcon

/**
 * Line marker for the package-name key in the mapping.yaml. It navigates to any folder with the same package name.
 */
class TypeMappingPackageLineMarker : RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    @Suppress("UnstableApiUsage")
    class Renderer(private val userDataKey: Key<String>) : PsiTargetPresentationRenderer<PsiElement>() {

        override fun getContainerText(element: PsiElement): String? {
            return getSymbolContainerText(element)
        }

        override fun getElementText(element: PsiElement): String {
            return getSymbolPresentableText(element)
        }

        override fun getIcon(element: PsiElement): JIcon? {
            return element.getIcon(Iconable.ICON_FLAG_VISIBILITY)
        }

        override fun getPresentation(element: PsiElement): TargetPresentation {
            if (element !is PsiDirectory) {
                return super.getPresentation(element)
            }

            val moduleLocation = getModuleLocation(element)
            val packageLocation = element.getUserData(userDataKey)
            val locationText = if (packageLocation == null) {
                moduleLocation?.text
            } else {
                "$packageLocation     ${moduleLocation?.text}"
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

    class GotoPackage(element: PsiDirectory, private val userDataKey: Key<String>):
        GotoRelatedItem(element, Goto.I18n.GROUP) {

        override fun getCustomName(): String {
            return getSymbolPresentableText(this.element!!)
        }

        override fun getCustomContainerName(): String {
            val loc = element!!.getUserData(userDataKey)!!
            val pkg = getSymbolContainerText(element!!)!!
            return "$pkg - $loc"
        }
    }

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

        if (element.text == PACKAGE_NAME_KEY) {
            val info = createLineMarkerInfo(element, PACKAGE_NAME_USER_KEY)
            if (info != null) {
                result.add(info)
            }
        }

        if (element.text == PACKAGE_BASE_KEY) {
            // todo check package-names parent key
            val info = createLineMarkerInfo(element, PACKAGE_NAMES_BASE_USER_KEY)
            if (info != null) {
                result.add(info)
            }
        }

        if (element.text == PACKAGE_LOCATION_KEY) {
            // todo check package-names parent key
            val info = createLineMarkerInfo(element, PACKAGE_NAMES_LOCATION_USER_KEY)
            if (info != null) {
                result.add(info)
            }
        }
    }

    private fun createLineMarkerInfo(packageKey: PsiElement, userDataKey: Key<String>): RelatedItemLineMarkerInfo<*>? {
        val packageKeyValue = packageKey.parent
        if (packageKeyValue !is YAMLKeyValue)
            return null

        val pkgName = packageKeyValue.valueText
        val module = findModule(packageKeyValue.containingFile)
            ?: return null

        val pkgDirs = service<TargetPackageService>()
            .findPackageDirs(pkgName, module)

        val targets = addLocations(pkgDirs, userDataKey)
        if (targets.isEmpty()) {
            log.warn("found no targets!")
            return null
        }

        val builder = NavigationGutterIconBuilder
            .create<PsiDirectory>(
                Icon.`package`,
                { listOf(it) },
                { listOf(GotoPackage(it, userDataKey)) })
            .setTooltipText(I18n.TOOLTIP_TEXT)
            .setPopupTitle(I18n.POPUP_TITLE)
            .setTargets(targets)
            .setTargetRenderer { Renderer(userDataKey) }

        return builder.createLineMarkerInfo(packageKey)
    }

    private fun addLocations(pkgDirs: List<PsiDirectory>, userDataKey: Key<String>): List<PsiDirectory> {
        if (pkgDirs.size < 2) {
            return pkgDirs
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
                it.putUserData(
                    userDataKey,
                    it.virtualFile.path.drop(commonPrefix.length).dropLast(commonSuffix.length)
                )
                it
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

    object Icon {
        val `package` = AllIcons.Modules.GeneratedFolder
    }

    object I18n {
        val TOOLTIP_TEXT = i18n("line.marker.type.mapping.package.tooltip")
        val POPUP_TITLE = i18n("line.marker.type.mapping.package.title")
    }

    companion object {
        const val PACKAGE_NAME_KEY = "package-name"
        //const val PACKAGE_NAMES_KEY = "package-names"
        const val PACKAGE_BASE_KEY = "base"
        const val PACKAGE_LOCATION_KEY = "location"
    }
}

private val PACKAGE_NAME_USER_KEY: Key<String> = Key.create("openapiprocessor.package-name")
private val PACKAGE_NAMES_BASE_USER_KEY: Key<String> = Key.create("openapiprocessor.package-names.base")
private val PACKAGE_NAMES_LOCATION_USER_KEY: Key<String> = Key.create("openapiprocessor.package-names.location")
