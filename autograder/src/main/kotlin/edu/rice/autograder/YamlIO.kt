/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.Try
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "YamlExporter"

private val header = "# THIS FILE IS AUTOMATICALLY GENERATED (%s), DO NOT EDIT!\n".format(
        ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.RFC_1123_DATE_TIME))

/** General-purpose Jackson YAML mapper. */
val kotlinYamlMapper = YAMLMapper()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerKotlinModule()

/**
 * Given a project name, as specified with a [GGradeProject] annotation,
 * and a Java code package name, specifying which Java (or Kotlin) code
 * packages should be searched for `@Grade` annotations, this scans for
 * the project grading specifications and produces a human-readable
 * (YAML) version of the policy. Useful for debugging.
 */
fun yamlExporter(
    projectName: String,
    codePackage: String,
    suppressDoNotEditLine: Boolean = false
): String {
    val scan = scanEverything(codePackage)
    val project = scan[projectName] ?: Log.ethrow(TAG, "Unknown project: $projectName")
    return project.yamlExporter(suppressDoNotEditLine)
}

/**
 * Given a [GGradeProject], extracts a string in YAML format, suitable for
 * printing to the user or saving in a file. Normally, a header comment
 * is generated saying "DO NOT EDIT". If you want to suppress that,
 * set the [suppressDoNotEditLine] parameter to *true*.
 */
fun GGradeProject.yamlExporter(suppressDoNotEditLine: Boolean = false) =
    (if (suppressDoNotEditLine) "" else header) +
        (kotlinYamlMapper.writeValueAsString(this)
            ?: Log.ethrow(TAG, "Jackson YAML failure?!"))

/**
 * Given a string in YAML format, tries to produce a [GGradeProject]
 * corresponding to the YAML file.
 */
fun yamlImporter(input: String): Try<GGradeProject> = Try {
    kotlinYamlMapper.readValue<GGradeProject>(input)
}
