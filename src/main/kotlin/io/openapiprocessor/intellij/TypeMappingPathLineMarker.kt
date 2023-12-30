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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.IconUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TypeMappingPathLineMarker : RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

        if (element !is YAMLKeyValue)
            return

        if(element.keyText != "paths")
            return

        val pathTargetService = service<PathTargetService>()

        // TODO for each path, handle http methods?
        element.value?.children?.forEach { path ->
            if (path !is YAMLKeyValue)
                return@forEach

            val isPath = path.keyText.startsWith("/")
            if (!isPath) {
                log.warn("expected to find path but found {}", path.keyText)
            }

            val targets = pathTargetService.findPathTargets(path.project, path.keyText)

            val builder = NavigationGutterIconBuilder
                .create(Support.ICON)
                .setTooltipTitle("OpenAPI Processor")
                .setTooltipText(TOOLTIP_TEXT)
                .setPopupTitle(POPUP_TITLE)
                .setEmptyPopupText("Could not find interface")
                .setTargets(targets)
            result.add(builder.createLineMarkerInfo(path.key!!))
        }
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        // static method on TypeMappingFileType
        return file.viewProvider.fileType is TypeMappingFileType
    }

    object Support {
        val ICON = IconUtil.scale(AllIcons.Nodes.Interface, null, 0.875f)
    }

    companion object {
        const val TOOLTIP_TEXT = "Navigate to endpoint interface methods"
        const val POPUP_TITLE = "Endpoint Interface Methods"
    }
}

