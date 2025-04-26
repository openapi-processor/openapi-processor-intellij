/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.navigation.GotoRelatedItem
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolContainerText
import com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolPresentableText


class GotoMethod(element: PsiElement): GotoRelatedItem(element, Goto.I18n.GROUP) {
    override fun getCustomName(): String {
        return getSymbolPresentableText(this.element!!)
    }

    override fun getCustomContainerName(): String {
        return getSymbolContainerText(this.element!!)!!
    }
}

object Goto {
    object I18n {
        val GROUP = i18n("goto.related.item.group")
    }
}
