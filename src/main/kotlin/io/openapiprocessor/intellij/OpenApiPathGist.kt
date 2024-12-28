/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.gist.GistManager
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("io.openapiprocessor.intellij.OpenApiPathGist")

class YamlKeyWithFile(val key: String, val offset: Int, val file: VirtualFile)

fun lookupKey(file: PsiFile): List<YamlKeyWithFile> {
    log.debug("looking for keys in {}", file.virtualFile.path)

    return OPENAPI_KEYS_GIST.getFileData(file)
      .map { yamlKey ->
          log.debug(">> found key: {}", yamlKey.key)
          YamlKeyWithFile(yamlKey.key, yamlKey.offset, file.virtualFile)
      }
}

private val OPENAPI_KEYS_GIST = GistManager.getInstance()
    .newPsiFileGist(
        "openapi-processor-intellij:openapi-keys",
        1,
        YamlKeyExternalizer(),
        ::findKeys)

private fun findKeys(file: PsiFile): List<YamlKey> {
    fun isPathKey(key: String): Boolean {
        return key.startsWith("paths") && countKeySeparators(key) == 1
    }

    if (file !is YAMLFile)
        return emptyList()

    log.debug("indexing file: {}, ", file.virtualFile.path)

    val keys = mutableListOf<YamlKey>()

    file.accept(object : YamlRecursivePsiElementVisitor() {
        override fun visitKeyValue(keyValue: YAMLKeyValue) {
            val key = keyValue.key
            if (key != null) {
                val fullKey = YAMLUtil.getConfigFullName(keyValue)
                if (isPathKey(fullKey)) {
                    log.debug(">> adding key: {}", fullKey)
                    keys.add(YamlKey(fullKey, key.textOffset))
                }
            }
            super.visitKeyValue(keyValue)
        }
    })

    return keys
}
