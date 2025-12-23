#!/usr/bin/env just --justfile

build:
  ./gradlew -x test build

dependencies:
  ./gradlew -q dependencies --configuration compileClasspath > ./dependencies.txt

# Report up-to-date dependencies by com.github.ben-manes.versions
updates:
  ./gradlew dependencyUpdates > updates.txt

insight:
  .gradlew dependencyInsight --dependency commons-codec --configuration scm

wrapper:
  ./gradlew wrapper --gradle-version=8.13


remotes:
  git remote -v

upstream:
    git fetch upstream
# git fetch --all

# merge template changes
upstream-merge:
    git merge upstream/main --allow-unrelated-histories
