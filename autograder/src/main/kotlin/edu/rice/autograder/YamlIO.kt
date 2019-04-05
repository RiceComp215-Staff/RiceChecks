//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import arrow.core.Try
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

private const val TAG = "YamlExporter"

/** General-purpose Jackson YAML mapper. */
val kotlinYamlMapper = YAMLMapper()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerKotlinModule()

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
    val project = scan[projectName] ?: Log.ethrow(TAG, "Unknown project: $projectName")
    return yamlExporter(project)
}

fun yamlExporter(project: GGradeProject) =
    kotlinYamlMapper.writeValueAsString(project) ?: Log.ethrow(TAG, "Jackson YAML failure?!")

fun yamlImporter(input: String): Try<GGradeProject> = Try {
    kotlinYamlMapper.readValue<GGradeProject>(input)
}
