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
                    .map { googleJavaStyleParser(it).eval() }
                    .getOrDefault { googleJavaStyleMissing }
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

    return if (testResultFiles.none()) {
        listOf(EvaluatorResult(false, 0.0, "No unit tests found!", emptyList()))
    } else {
        // TODO: inline this into one expr after we're sure we won't want to step through here with a debugger
        val parsedResults = testResultFiles
                .map { junitSuiteParser(it) }.toList()
        val evalResults = parsedResults.eval(this)
        evalResults
    }
}

fun GGradeProject.jacocoAggregator(): List<EvaluatorResult> =
        listOf(if (coveragePoints == 0.0)
            passingEvaluatorResult(0.0, "No code coverage requirements")
        else
            readFile("${AutoGrader.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
                    .map { jacocoParser(it).eval(this) }
                    .getOrDefault { jacocoResultsMissing })

fun GGradeProject.allResults(): List<EvaluatorResult> =
        unitTestAggregator() + warningAggregator() +
                if (coveragePoints == 0.0) emptyList() else jacocoAggregator()

private const val lineLength = 71
private const val leftColumn = 57
private const val rightColumn = 8
private const val checkMark = "✅" // fixed-width font note: we have no guarantees about Emoji widths
private const val failMark = "❌"
/**
 * Prints everything to the given output [PrintStream], then returns
 * whether the tests succeeded (true) or failed (false).
 */
fun GGradeProject.printResults(stream: PrintStream, results: List<EvaluatorResult>): Boolean {
    // Engineering note: inside each topic, we start from the maximum score and then subtract
    // deductions, with a floor at zero. When producing the final grade, we then add together
    // all the individual topic scores.

    val blankLine = "│" // unicode: "BOX DRAWINGS LIGHT VERTICAL"
    val dividerLine = "├" + "─".repeat(lineLength - 1)
    val startDividerLine = "┌" + "─".repeat(lineLength - 1)
    val endDividerLine = "└" + "─".repeat(lineLength - 1)

    stream.println(startDividerLine)
    stream.println("$blankLine %-${lineLength - 2}s".format("Autograder for $name"))
    wordWrap(description, lineLength - 2).forEach {
        stream.println("$blankLine %-${lineLength - 2}s".format(it))
    }
    stream.println(dividerLine)
    stream.println(blankLine)
    results.forEach { (passes, points, title, deductions) ->
        val emoji = if (passes) checkMark else failMark
        stream.println("$blankLine %-${leftColumn}s %$rightColumn.1f $emoji".format(title, points))
        deductions.forEach { (name, value) ->
            val wrapped = wordWrap(name, leftColumn - 2)
            stream.println("$blankLine - %-${leftColumn - 2}s %$rightColumn.1f".format(wrapped[0], if (value != 0.0) -value else 0.0))
            wrapped.tail().forEach {
                stream.println("$blankLine   $it")
            }
        }
        stream.println(blankLine)
    }

    val allPassing = results.fold(true) { a, b -> a && b.passes }
    val emoji = if (allPassing) checkMark else failMark
    val allPoints = results.sumByDouble { it.points }

    stream.println(dividerLine)
    stream.println("$blankLine Total points: %.1f/%.1f $emoji".format(allPoints, maxPoints))
    stream.println(endDividerLine)

    return allPassing
}
