/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.project.Project
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.UsageSearchContext
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile

const val YAML_KEY_SEPARATOR = '.'

data class YamlKey(val key: String, val offset: Int)

fun countKeySeparators(key: String): Int {
  return key.count { it == YAML_KEY_SEPARATOR }
}

fun findPathInYaml(path: String, searchScope: GlobalSearchScope, project: Project): List<YamlKeyWithFile> {
  val yamlSearchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(searchScope, YAMLFileType.YML)

  return findYamlFilesWithPath(path, yamlSearchScope, project)
      .distinct()
      .flatMap(::lookupKey)
      .filter { key -> key.key == "paths.${path}" }
}

private fun findYamlFilesWithPath(path: String, searchScope: GlobalSearchScope, project: Project): List<YAMLFile> {
    return CacheManager
        .getInstance(project)
        .getFilesWithWord(path, UsageSearchContext.ANY, searchScope, false)
        .filterIsInstance<YAMLFile>()
}
