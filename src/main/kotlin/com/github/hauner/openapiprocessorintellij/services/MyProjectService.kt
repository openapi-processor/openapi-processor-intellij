package com.github.hauner.openapiprocessorintellij.services

import com.github.hauner.openapiprocessorintellij.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
