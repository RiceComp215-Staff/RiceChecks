//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.getOrDefault
import arrow.syntax.collections.tail
import java.io.PrintStream

fun GGradeProject.warningAggregator(): List<EvaluatorResult> =
        listOf(if (warningPoints == 0.0) {
            passingEvaluatorResult(0.0, "No warning / style deductions")
        } else {
            Log.i("warningAggregator", "useCheckStyle($useCheckStyle), useGoogleJavaFormat($useGoogleJavaFormat), useJavacWarnings($useJavacWarnings)")

            val googleJavaStyleContents = readFile("${AutoGrader.buildDir}/google-java-format/0.8/fileStates.txt")
                    .map { googleJavaFormatParser(it).eval() }
                    .getOrDefault { googleJavaFormatMissing }
            val checkStyleMainContents = readFile("${AutoGrader.buildDir}/reports/checkstyle/main.xml")
                    .map { checkStyleParser(it).eval("main") }
                    .getOrDefault { checkStyleMissing("main") }
            val checkStyleTestContents = readFile("${AutoGrader.buildDir}/reports/checkstyle/test.xml")
                    .map { checkStyleParser(it).eval("test") }
                    .getOrDefault { checkStyleMissing("test") }
            val compilerLogContents = readFile("${AutoGrader.buildDir}/logs/compile.log")
                    .map { javacZeroWarnings(it) }
                    .getOrDefault { javacLogMissing }

            val checkStyleMaybe =
                    if (useCheckStyle) listOf(checkStyleMainContents, checkStyleTestContents) else emptyList()
            val googleJavaStyleMaybe =
                    if (useGoogleJavaFormat) listOf(googleJavaStyleContents) else emptyList()
            val compilerMaybe =
                    if (useJavacWarnings) listOf(compilerLogContents) else emptyList()

            val allResults = checkStyleMaybe + googleJavaStyleMaybe + compilerMaybe

            val passing = allResults.fold(true) { a, b -> a && b.second }

            EvaluatorResult(passing,
                    if (passing) warningPoints else 0.0,
                    warningPoints,
                    if (passing) "No warning / style deductions" else "Warning / style deductions",
                    allResults.map { it.first to if (it.second) 0.0 else warningPoints })
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
        listOf(EvaluatorResult(false, 0.0, this.maxPoints, "No unit tests found!", emptyList()))
    } else {
        // TODO: inline this into one expr after we're sure we won't want to step through here with a debugger
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
            passingEvaluatorResult(0.0, "No code coverage requirements")
        else
            readFile("${AutoGrader.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
                    .map { jacocoParser(it).eval(this) }
                    .getOrDefault { jacocoResultsMissing() })

fun GGradeProject.allResults(): List<EvaluatorResult> =
        unitTestAggregator() + warningAggregator() +
                if (coveragePoints == 0.0) emptyList() else jacocoAggregator()

private const val lineLength = 78
private const val rightColumn = 8
private const val leftColumn = lineLength - rightColumn - 6
private const val checkMark = "✅" // fixed-width font note: we have no guarantees about Emoji widths
private const val failMark = "❌"
private const val blankLine = "│" // unicode: "BOX DRAWINGS LIGHT VERTICAL"
private val dividerLine = "├" + "─".repeat(lineLength - 1)
private val startDividerLine = "┌" + "─".repeat(lineLength - 1)
private val endDividerLine = "└" + "─".repeat(lineLength - 1)

private fun Double.rightColumnNonZero() = if (this == 0.0) "" else "%${rightColumn}s".format("(%.1f)".format(this))
private fun Double.rightColumn() = "%${rightColumn - 1}.1f ".format(this)
private fun fractionLine(detail: String, top: Double, bottom: Double, passing: Boolean): String {
    val emoji = if (passing) checkMark else failMark
    val fraction = "%.1f/%.1f".format(top, bottom)
    return "$blankLine %-${leftColumn - rightColumn - 2}s %${rightColumn * 2 + 1}s $emoji".format(detail, fraction)
}

/**
 * Prints everything to the given output [PrintStream], then returns
 * whether the tests succeeded (true) or failed (false).
 */
fun GGradeProject.printResults(stream: PrintStream, results: List<EvaluatorResult>): Boolean {
    // Engineering note: inside each topic, we start from the maximum score and then subtract
    // deductions, with a floor at zero. When producing the final grade, we then add together
    // all the individual topic scores.

    stream.println(startDividerLine)
    stream.println("$blankLine %-${lineLength - 2}s".format("Autograder for $name"))
    wordWrap(description, lineLength - 2).forEach {
        stream.println("$blankLine %-${lineLength - 2}s".format(it))
    }
    stream.println(dividerLine)
    stream.println(blankLine)
    results.forEach { (passes, points, maxPoints, title, deductions) ->
        stream.println(fractionLine(title, points, maxPoints, passes))
        deductions.forEach { (text, value) ->
            // newlines are optional
            val wrapped = text.split("\n").flatMap {
                wordWrap(it, leftColumn - 2)
            }

            stream.println("$blankLine - %-${leftColumn - 2}s %s".format(wrapped[0], (-value).rightColumnNonZero()))
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

    return allPassing
}
