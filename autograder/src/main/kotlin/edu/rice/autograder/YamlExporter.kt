//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.lang.RuntimeException

/**
 * Given a project name, as specified with a [GradeProject] annotation,
 * and a Java code package name, specifying which Java (or Kotlin) code
 * packages should be searched for Grade annotations, this scans for
 * the project grading specifications and produces a human-readable
 * (YAML) version of the policy.
 *
 * This YAML isn't particularly meant to be read back in again, such
 * as for use in the Illinois autograder, but it's handy when trying
 * to have a human-readable version of our data.
 */
fun yamlExporter(projectName: String, codePackage: String): String {
    val scan = scanEverything(codePackage)
    val project = scan[projectName] ?: throw RuntimeException("Unknown project: $projectName")

    return YAMLMapper().writeValueAsString(project) ?: throw RuntimeException("Jackson YAML failure?!")
}

