/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.components.Service

@Service
class TargetPackageService(finder: TargetPackageFinder = TargetPackageFinderImpl()): TargetPackageFinder by finder
