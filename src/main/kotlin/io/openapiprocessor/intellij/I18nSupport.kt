/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val BUNDLE = "messages.I18nBundle"

private object I18nMessages {
    val bundle = DynamicBundle(this::class.java, BUNDLE)
}

fun i18n(@PropertyKey(resourceBundle = BUNDLE) key: String): String {
    return I18nMessages.bundle.getMessage(key)
}

@Suppress("unused")
fun i18nLazy(@PropertyKey(resourceBundle = BUNDLE) key: String): Supplier<String> {
    return I18nMessages.bundle.getLazyMessage(key)
}

@Suppress("unused")
fun i18n(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any?): String {
    return I18nMessages.bundle.getMessage(key, *params)
}
