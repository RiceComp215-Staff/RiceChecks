/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.Try
import com.fasterxml.jackson.module.kotlin.readValue

private const val TAG = "YamlExporter"

/**
 * Given a project name, as specified with a [GGradeProject] annotation,
 * and the output of running [scanEverything], this scans for
 * the project grading specifications and produces a human-readable
 * (YAML) version of the policy. Useful for debugging.
 */
fun Map<String, GGradeProject>.yamlExporter(
    projectName: String,
    suppressDoNotEditLine: Boolean = false
): String {
    val project = this[projectName] ?: Log.ethrow(TAG, "Unknown project: $projectName")
    return project.yamlExporter(suppressDoNotEditLine)
}

/**
 * Given a [GGradeProject], extracts a string in YAML format, suitable for
 * printing to the user or saving in a file. Normally, a header comment
 * is generated saying "DO NOT EDIT". If you want to suppress that,
 * set the [suppressDoNotEditLine] parameter to *true*.
 */
fun GGradeProject.yamlExporter(suppressDoNotEditLine: Boolean = false) =
    (if (suppressDoNotEditLine) "" else yamlHeader) +
        (jacksonYamlMapper.writeValueAsString(this)
            ?: Log.ethrow(TAG, "Jackson YAML failure?!"))

/**
 * Given a string in YAML format, tries to produce a [GGradeProject]
 * corresponding to the YAML file.
 */
fun yamlImporter(input: String): Try<GGradeProject> = Try {
    jacksonYamlMapper.readValue<GGradeProject>(input)
}
