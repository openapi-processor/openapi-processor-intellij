# openapi-processor-intellij

[![Build](https://github.com/openapi-processor/openapi-processor-intellij/actions/workflows/build.yml/badge.svg)](https://github.com/openapi-processor/openapi-processor-intellij/actions/workflows/build.yml)
[![Version](https://img.shields.io/jetbrains/plugin/v/17008.svg)](https://plugins.jetbrains.com/plugin/17008)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17008.svg)](https://plugins.jetbrains.com/plugin/17008)

## Plugin Description
<!-- Plugin description -->
Adds support for the [openapi-processor](https://openapiprocessor.io/oap/home/home.html) `mapping.yaml` configuration file.
<br/><br/>

* automatically selects the json schema of the `mapping.yaml` to provide editing support (autocompletion & validation). Note that the `openapi-processor-mapping: v[x]` line in the `mapping.yaml` is used to detect it. 
* marks the `mapping.yaml` with an openapi-processor icon to separate it from the openapi yaml file(s) (includes new ui icon)
* navigate from the `package-name` configuration in the `mapping.yaml` to the package with the generated sources (if the target package exists).
* navigate from an endpoint `path` mapping in the `mapping.yaml` to the generated interface method (via a standard interface icon).  
* navigate from an OpenAPI endpoint `path` to the generated endpoint interface methods (via a standard interface icon).
* navigate from a generated endpoint interface to the OpenAPI endpoint `path` (via a standard OpenAPI icon).
<br/><br/>
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "openapi-processor"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/openapi-processor/openapi-processor-intellij/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Logging

* to enable debug logging add "io.openapiprocessor.intellij:trace" into Help/Diagnostic Tools/Debug Log Settings...


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
