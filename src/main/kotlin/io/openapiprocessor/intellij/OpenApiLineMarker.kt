package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OpenApiLineMarker: RelatedItemLineMarkerProvider()  {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {

    }

    object Methods {
        val methods = listOf("delete", "get", "head", "patch", "post", "put", "trace")
    }
}
