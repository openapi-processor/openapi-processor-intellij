<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# openapi-processor-intellij Changelog

## [Unreleased]

## [2024.3-SNAPSHOT.2]

- simpler targetDir lookup

## [2024.3-SNAPSHOT.1]

- build maintenance

## [2024.2]

- IDEA 2024.3 compatibility

## [2024.2-SNAPSHOT.1]

- IDEA 2024.3 compatibility

## [2024.1]

- dynamic detection of `mapping.yaml` json schema.<br> i.e. it is no longer necessary to wait for a new version that supports the new mapping schema version. This removes the openapi-processor schemas from IntelliJ's json mappings.
- navigate from an OpenAPI path to the generated interface methods. <br> The plugin will add an interface icon to the paths in an OpenAPI yaml document that provides navigation to the generated interface methods. 
- clean up icons, new ui icons. <br> navigation from a path mapping in the `mapping.yaml` now uses the interface icon. Added a new ui icon for the `mapping.yaml`.

## [2023.3-SNAPSHOT.3]

- dynamic detection of `mapping.yaml` json schema, i.e. it is no longer necessary to publish a new version for each mapping schema update.
- navigate from openapi path to the generated interface methods
- clean up icons, new ui icons

## [2023.3-SNAPSHOT.2]

- support mapping format v5 (since openapi-processor-spring/micronaut 2023.6)
- navigate from openapi path to the generated interface methods
- clean up icons, new ui icons

## [2023.3-SNAPSHOT.1]

- support mapping format v5 (since openapi-processor-spring/micronaut 2023.6)

## [2023.2]

- support mapping format v4 (since openapi-processor-spring/micronaut 2023.3)

## [2023.2-SNAPSHOT.1]

- support mapping format v4 (since openapi-processor-spring/micronaut 2023.3)

## [2023.1]

- support mapping format v3 (since openapi-processor-spring/micronaut 2023.1)

## [2023.1-SNAPSHOT.1]

- support mapping format v3 (since openapi-processor-spring/micronaut 2023.1)

## [2022.4]

- Idea 2022.3 compatibility
- allow future Idea builds
- increased minimum version to Idea 2020.3

## [2022.3]

- support mapping format v2.1 (since openapi-processor-spring/micronaut 2022.5)

## [2022.3-SNAPSHOT.1]

- support mapping format v2.1 (since openapi-processor-spring/micronaut 2022.5)

## [2022.2.1]

- Idea 2022.2 compatibility

## [2022.1]

- same as 2022.1-SNAPSHOT.1

## [2022.1-SNAPSHOT.1]

- 2022.1 compatibility

## [2021.1-SNAPSHOT.6]

- do not log 'file not found' as error.

## [2021.1-SNAPSHOT.5]

- fixed exception if the target package does not exist.

## [2021.1-SNAPSHOT.4]

- navigate from path mapping to interface method (spring & micronaut).

## [2021.1-SNAPSHOT.3]

- fix exception in file type check.

## [2021.1-SNAPSHOT.2]

- auto-detect mapping.yaml by `openapi-processor-mapping: v2` key/value instead of matching the file name.
- add navigation gutter icon on `package-name` configuration in the `mapping.yaml`. If the target package exists, it will navigate to the package with the generated sources.

## [2021.1-SNAPSHOT.1]

- auto-detect the `mapping.yaml` file and set the json schema for editing support (autocompletion & validation)
- add openapi-processor icon for `mapping.yaml`

[Unreleased]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2024.3-SNAPSHOT.1...HEAD
[2022.2.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.1...v2022.2.1
[2021.1-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/commits/v2021.1-SNAPSHOT.1
[2021.1-SNAPSHOT.2]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.1...v2021.1-SNAPSHOT.2
[2021.1-SNAPSHOT.3]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.2...v2021.1-SNAPSHOT.3
[2021.1-SNAPSHOT.4]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.3...v2021.1-SNAPSHOT.4
[2021.1-SNAPSHOT.5]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.4...v2021.1-SNAPSHOT.5
[2021.1-SNAPSHOT.6]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.5...v2021.1-SNAPSHOT.6
[2022.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.1-SNAPSHOT.1...v2022.1
[2022.1-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2021.1-SNAPSHOT.6...v2022.1-SNAPSHOT.1
[2022.3]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.3-SNAPSHOT.1...v2022.3
[2022.3-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.2.1...v2022.3-SNAPSHOT.1
[2022.4]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.3...v2022.4
[2023.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.1-SNAPSHOT.1...v2023.1
[2023.1-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2022.4...v2023.1-SNAPSHOT.1
[2023.2]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.2-SNAPSHOT.1...v2023.2
[2023.2-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.1...v2023.2-SNAPSHOT.1
[2023.3-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.2...v2023.3-SNAPSHOT.1
[2023.3-SNAPSHOT.2]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.3-SNAPSHOT.1...v2023.3-SNAPSHOT.2
[2023.3-SNAPSHOT.3]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.3-SNAPSHOT.2...v2023.3-SNAPSHOT.3
[2024.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2023.3-SNAPSHOT.3...v2024.1
[2024.2]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2024.2-SNAPSHOT.1...v2024.2
[2024.2-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2024.1...v2024.2-SNAPSHOT.1
[2024.3-SNAPSHOT.1]: https://github.com/openapi-processor/openapi-processor-intellij/compare/v2024.2...v2024.3-SNAPSHOT.1
