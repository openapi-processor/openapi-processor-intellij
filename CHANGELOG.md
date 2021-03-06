<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# openapi-processor-intellij Changelog

## [Unreleased]
## [2022.2.1]
- 2022.2 compatibility

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
