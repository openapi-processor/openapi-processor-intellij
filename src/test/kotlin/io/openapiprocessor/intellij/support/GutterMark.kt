/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod

val GutterMark.methods: List<PsiElement>
    get() = getMethods(getHandler(this))


fun getTargets(gutterMark: GutterMark): List<String> {
    return getTargets(getHandler(gutterMark))
}

private fun getTargets(handler: NavigationGutterIconRenderer): List<String> {
    val targets = mutableListOf<String>()

    handler.targetElements.forEach {
        it.accept(object : PsiElementVisitor() {
            override fun visitDirectory(dir: PsiDirectory) {
                targets.add(dir.virtualFile.path)
            }
        })
    }

    return targets
}

private fun getMethods(handler: NavigationGutterIconRenderer): List<PsiMethod> {
    return handler.targetElements.filterIsInstance(PsiMethod::class.java)
}

private fun getHandler(gutter: GutterMark): NavigationGutterIconRenderer {
    if (gutter !is LineMarkerInfo.LineMarkerGutterIconRenderer<*>) {
        throw AssertionError("bad gutter mark!")
    }

    val handler = gutter.lineMarkerInfo.navigationHandler
    if (handler !is NavigationGutterIconRenderer) {
        throw AssertionError("bad navigation handler!")
    }

    return handler
}
