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
            val googleJavaStyleContents = readFile("build/google-java-format/0.8/fileStates.txt")
                    .map { googleJavaStyleParser(it).eval() }
                    .getOrDefault { googleJavaStyleMissing }
            val checkStyleMainContents = readFile("build/reports/checkstyle/main.xml")
                    .map { checkStyleParser(it).eval("main") }
                    .getOrDefault { checkStyleMissing("main") }
            val checkStyleTestContents = readFile("build/reports/checkstyle/test.xml")
                    .map { checkStyleParser(it).eval("test") }
                    .getOrDefault { checkStyleMissing("test") }
            val compilerLogContents = readFile("build/logs/compile.log")
                    .map { javacZeroWarnings(it) }
                    .getOrDefault { javacLogMissing }

            val allResults = listOf(googleJavaStyleContents,
                    checkStyleMainContents,
                    checkStyleTestContents,
                    compilerLogContents)

            val passing = allResults.fold(true) { a, b -> a && b.second }

            EvaluatorResult(passing,
                    if (passing) warningPoints else 0.0,
                    if (passing) "No warning / style deductions" else "Warning / style deductions",
                    allResults.map { it.first to if (it.second) warningPoints else 0.0 })
        })

fun GGradeProject.unitTestAggregator(): List<EvaluatorResult> {
    val testResultFiles = readdirPath("build/test-results/test")
            .onFailure {
                Log.e("unitTestAggregator", "Failed to read test-results directory!", it)
            }.getOrDefault { emptySequence() }
            .flatMap { it.readFile().asSequence() }

    if (testResultFiles.none()) {
        return listOf(EvaluatorResult(false, 0.0, "No unit tests found!", emptyList()))
    } else {
        val parsedResults = testResultFiles.map { junitSuiteParser(it) }.toList()
        val tmp = parsedResults.eval(this)
        return tmp
    }
}

fun GGradeProject.jacocoAggregator(): List<EvaluatorResult> =
        listOf(if (coveragePoints == 0.0)
            passingEvaluatorResult(0.0, "No code coverage requirements")
        else
            readFile("build/reports/jacoco/test/jacocoTestReport.xml")
                    .map { jacocoParser(it).eval(this) }
                    .getOrDefault { jacocoResultsMissing })

fun GGradeProject.allResults(): List<EvaluatorResult> =
        unitTestAggregator() + warningAggregator() +
                if (coveragePoints == 0.0) emptyList() else jacocoAggregator()

private const val lineLength = 71
private const val leftColumn = 57
private const val rightColumn = 8
private const val checkMark = "✅" // fixed-width font note: these are 2 chars wide
private const val failMark = "❌"
/**
 * Prints everything to the given output [PrintStream], then returns
 * whether the tests succeeded (true) or failed (false).
 */
fun GGradeProject.printResults(stream: PrintStream, results: List<EvaluatorResult>): Boolean {
    val blankLine = "="
    val dividerLine = "=".repeat(lineLength)
    stream.println(dividerLine)
    stream.println("= %-${lineLength - 2}s".format("Autograder for $name"))
    stream.println(blankLine)
    wordWrap(description, lineLength - 2).forEach {
        stream.println("= %-${lineLength - 2}s".format(it))
    }
    stream.println(blankLine)
    results.forEach { (passes, points, title, deductions) ->
        val emoji = if (passes) checkMark else failMark
        stream.println("= %-${leftColumn}s %$rightColumn.1f $emoji".format(title, points))
        deductions.forEach { (name, value) ->
            val wrapped = wordWrap(name, leftColumn - 2)
            stream.println("= - %-${leftColumn - 2}s %$rightColumn.1f".format(wrapped[0], -value))
            wrapped.tail().forEach {
                stream.println("=   $it")
            }
        }
    }

    val allPassing = results.fold(true) { a, b -> a && b.passes }
    val emoji = if (allPassing) checkMark else failMark
    val allPoints = results.sumByDouble { it.points }

    // For this one time, we're not doing the two columns
    stream.println(blankLine)
    stream.println("= Total points: %.1f/%.1f $emoji".format(allPoints, maxPoints))
    stream.println(dividerLine)

    return allPassing
}

// borrowed from rosettacode, then modified heavily
fun wordWrap(text: String, lineWidth: Int): List<String> {
    val result = emptyList<String>().toMutableList()
    val words = text.split(' ')
    var sb = StringBuilder(words[0])
    var spaceLeft = lineWidth - words[0].length

    for (word in words.tail()) {
        val len = word.length
        if (len + 1 > spaceLeft) {
            result.add(sb.toString())
            sb = StringBuilder(word)
            spaceLeft = lineWidth - len
        } else {
            sb.append(" ").append(word)
            spaceLeft -= (len + 1)
        }
    }
    result.add(sb.toString())
    return result.toList() // make immutable
}
