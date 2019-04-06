//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.getOrDefault

fun warningAggregator(project: GGradeProject): EvaluatorResult {
    if (project.warningPoints == 0.0) {
        return passingEvaluatorResult(0.0, "No warning / style deductions")
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

        return EvaluatorResult(passing,
                if (passing) project.warningPoints else 0.0,
                if (passing) "No warning / style deductions" else "Warning / style deductions",
                allResults.map { it.first to if (it.second) project.warningPoints else 0.0 })
    }
}

fun unitTestAggregator(project: GGradeProject): List<EvaluatorResult> {
    val testResultFiles = readdirPath("build/test-results/test")
            .onFailure {
                Log.e("unitTestAggregator", "Failed to read test-results directory!", it)
            }.getOrDefault { emptySequence() }
            .flatMap { it.readFile().asSequence() }

    if (testResultFiles.none()) {
        return listOf(EvaluatorResult(false, 0.0, "No unit tests found!", emptyList()))
    } else {
        val parsedResults = testResultFiles.map { junitSuiteParser(it) }.toList()
        val tmp = parsedResults.eval(project)
        return tmp
    }
}

fun jacocoAggregator(project: GGradeProject): EvaluatorResult {
    if (project.coveragePoints == 0.0)  {
        return passingEvaluatorResult(0.0, "No code coverage requirements")
    }

    return readFile("build/reports/jacoco/test/jacocoTestReport.xml")
            .map { jacocoParser(it).eval(project) }
            .getOrDefault { jacocoResultsMissing }
}
