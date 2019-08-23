/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.getOrDefault
import arrow.syntax.collections.tail
import java.io.ByteArrayOutputStream
import java.io.PrintStream

const val STYLE_CATEGORY = "Style"
const val COVERAGE_CATEGORY = "Coverage"
const val TESTS_CATEGORY = "Tests"

fun GGradeProject.warningAggregator(): List<EvaluatorResult> =
    listOf(if (warningPoints == 0.0) {
        passingEvaluatorResult(0.0, "No warning / style deductions", STYLE_CATEGORY)
    } else {
        Log.i("warningAggregator",
            "useCheckStyle($useCheckStyle), " +
                "useGoogleJavaFormat($useGoogleJavaFormat), " +
                "useJavacWarnings($useJavacWarnings)")

        val googleJavaFormatContents =
            readFile("${AutoGrader.buildDir}/google-java-format/0.8/fileStates.txt")
                .map { googleJavaFormatParser(it).eval() }
                .getOrDefault { googleJavaFormatMissing }

        val checkStyleMainContents =
            readFile("${AutoGrader.buildDir}/reports/checkstyle/main.xml")
                .map { checkStyleParser(it).eval("main") }
                .getOrDefault { checkStyleMissing("main") }

        val checkStyleTestContents =
            readFile("${AutoGrader.buildDir}/reports/checkstyle/test.xml")
                .map { checkStyleParser(it).eval("test") }
                .getOrDefault { checkStyleMissing("test") }

        val compilerLogContents =
            readFile("${AutoGrader.buildDir}/logs/compile.log")
                .map { javacZeroWarnings(it) }
                .getOrDefault { javacLogMissing }

        val checkStyleMaybe =
            if (useCheckStyle)
                listOf(checkStyleMainContents, checkStyleTestContents)
            else
                emptyList()

        val googleJavaStyleMaybe =
            if (useGoogleJavaFormat) listOf(googleJavaFormatContents) else emptyList()

        val compilerMaybe =
            if (useJavacWarnings) listOf(compilerLogContents) else emptyList()

        val allResults = checkStyleMaybe + googleJavaStyleMaybe + compilerMaybe

        val passing = allResults.fold(true) { a, b -> a && b.passing }

        EvaluatorResult(passing,
            if (passing) warningPoints else 0.0,
            warningPoints,
            if (passing) "No warning / style deductions" else "Warning / style deductions",
            STYLE_CATEGORY,
            allResults)
    })

fun GGradeProject.unitTestAggregator(): List<EvaluatorResult> {
    val testResultFiles = readdirPath("${AutoGrader.buildDir}/test-results/test")
            .onFailure {
                Log.e("unitTestAggregator", "Failed to read test-results directory!", it)
            }.getOrDefault { emptyList() }
            .filter { it.fileName.toString().endsWith(".xml") }
            .flatMap { it.readFile().asList() }

    Log.i("unitTestAggregator", "Found ${testResultFiles.size} files")

    return if (testResultFiles.isEmpty()) {
        Log.i("unitTestAggregator", "Yielded zero evaluation results!")
        listOf(EvaluatorResult(false, 0.0, this.maxPoints,
            "No unit tests found!", TESTS_CATEGORY, emptyList()))
    } else {
        val parsedResults = testResultFiles
                .map { junitSuiteParser(it) }
                .sortedBy { it.className }
        val evalResults = parsedResults.eval(this)
        Log.i("unitTestAggregator", "Yielded ${evalResults.size} evaluation results")
        evalResults
    }
}

fun GGradeProject.jacocoAggregator(): List<EvaluatorResult> =
        listOf(if (coveragePoints == 0.0)
            passingEvaluatorResult(0.0, "No code coverage requirements", COVERAGE_CATEGORY)
        else
            readFile("${AutoGrader.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
                    .map { jacocoParser(it).eval(this) }
                    .getOrDefault { jacocoResultsMissing() })

// Unicode note: Even though we're normally expecting our results to appear using
// a fixed-width font, the drawing symbols and the other Unicode stuff below is
// going to come elsewhere, meaning that we will have no assurance that we can
// vertically align text when one line has a funky symbol and the next line doesn't.
// This is why we're not trying to print a right-side border on the text box, while
// we're fine printing a top, left, and bottom border.

private const val lineLength = 78
private const val rightColumn = 8
private const val leftColumn = lineLength - rightColumn - 6
private const val goodMark = "✅"
private const val failMark = "❌"
private const val blankLine = "│" // unicode: "BOX DRAWINGS LIGHT VERTICAL"
private val dividerLine = "├" + "─".repeat(lineLength - 1)
private val startDividerLine = "┌" + "─".repeat(lineLength - 1)
private val endDividerLine = "└" + "─".repeat(lineLength - 1)

private fun Double.rightColumnNonZero() =
    if (this == 0.0) ""
    else "%${rightColumn}s".format("(%.1f)".format(this))

private fun fractionLine(detail: String, top: Double, bottom: Double, passing: Boolean): String {
    val emoji = if (passing) goodMark else failMark
    val fraction = "%.1f/%.1f".format(top, bottom)
    return "$blankLine %-${leftColumn - rightColumn - 2}s %${rightColumn * 2 + 1}s $emoji"
        .format(detail, fraction)
}

/**
 * This data structure represents the "final report" from the autograder. It's got
 * all the measurements and conclusions of the autograder in one place. The expected
 * methods for processing this are [ResultsReport.writeReports] and
 * [ResultsReport.humanReport].
 */
data class ResultsReport(
    val projectName: String,
    val description: String,
    val allPassing: Boolean,
    val points: Double,
    val maxPoints: Double,
    val results: List<EvaluatorResult>
)

/** Given a [GGradeProject], extracts a [ResultsReport]. */
fun GGradeProject.toResultsReport(): ResultsReport {
    val results = unitTestAggregator() +
        (if (warningPoints == 0.0) emptyList() else warningAggregator()) +
        (if (coveragePoints == 0.0) emptyList() else jacocoAggregator())

    val allPassing = results.fold(true) { a, b -> a && b.passes }
    val allPoints = results.sumByDouble { it.points }

    return ResultsReport(name, description, allPassing, allPoints, maxPoints, results)
}

/** Generates a human-readable report. */
fun ResultsReport.humanReport(): String {
    val bos = ByteArrayOutputStream()
    val stream = PrintStream(bos, true, "UTF-8")

    stream.println(startDividerLine)
    stream.println("$blankLine %-${lineLength - 2}s".format("Autograder for $projectName"))
    wordWrap(description, lineLength - 2).forEach {
        stream.println("$blankLine %-${lineLength - 2}s".format(it))
    }
    stream.println(dividerLine)
    stream.println(blankLine)
    results.forEach { (passes, points, maxPoints, title, _, deductions) ->
        stream.println(fractionLine(title, points, maxPoints, passes))
        deductions
            .filter { it.worthPrinting() }
            .forEach { (text, value) ->
                // we treat newlines as forced linebreaks then wrap each line
                val wrapped = text.split("\n").flatMap {
                    wordWrap(it, leftColumn - 2)
                }

                stream.println("$blankLine - %-${leftColumn - 2}s %s"
                    .format(wrapped[0], (-value).rightColumnNonZero()))
                wrapped.tail().forEach {
                    stream.println("$blankLine   $it")
                }
            }
        stream.println(blankLine)
    }

    val allPassing = results.fold(true) { a, b -> a && b.passes }
    val allPoints = results.sumByDouble { it.points }

    stream.println(dividerLine)
    stream.println(fractionLine("Total points:", allPoints, maxPoints, allPassing))
    stream.println(endDividerLine)

    return bos.toString("UTF-8")
}

/**
 * While we want "everything" in the machine-readable results, we'll filter the results
 * we print to the humans based on this predicate, so the report (mostly) just says
 * what went wrong rather than listing every single test that passes.
 */
fun Deduction.worthPrinting(): Boolean = when {
    this is UnitTestDeduction && cost == 0.0 -> false
    this is UnitTestFactoryDeduction && numChecked > 0 && numPassed == numChecked -> false
    this is CodeStyleDeduction && passing -> false
    else -> true
}

/**
 * Writes out two files in `build/autograder`, one in JSON format and one in YAML, representing
 * the contents of the [ResultsReport], suitable for subsequent processing, uploading, etc.
 * Also prints the human-readable report, via [ResultsReport.humanReport], to [System.out].
 */
fun ResultsReport.writeReports(quiet: Boolean = false) {
    val jsonData = jacksonJsonMapper
        .writer()
        .withDefaultPrettyPrinter()
        .writeValueAsString(this) ?: ""
    val yamlData = yamlHeader + (jacksonYamlMapper.writeValueAsString(this) ?: "")
    val txtData = humanReport()

    val jsonReport = "build/autograder/report.json"
    val yamlReport = "build/autograder/report.yml"
    val txtReport = "build/autograder/report.txt"

    writeFile(jsonReport, jsonData)
    writeFile(yamlReport, yamlData)
    writeFile(txtReport, txtData)

    // also, generates a report to stdout
    if (!quiet) print(txtData)
}
