#!/usr/bin/env just --justfile

default:
  @just --list --unsorted

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
    git merge upstream/HEAD --allow-unrelated-histories

upstream-rebase:
    git rebase -i upstream/next

# show upstream changes
upstream-changes:
    git log HEAD..upstream/main --oneline --reverse

# merge upstream changes until commit hash
merge hash:
    git merge {{hash}} --allow-unrelated-histories --no-commit --no-ff

# build plugin zip
build-plugin:
  ./gradlew buildPlugin


