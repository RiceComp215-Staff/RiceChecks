//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

internal fun warningAggregator(project: GGradeProject): EvaluatorResult {
    if(project.warningPoints == 0.0) {
        return passingEvaluatorResult(0.0, "No warning / style deductions")
    } else {
        val allResults = listOf(
                googleJavaStyleParser("build/google-java-format/0.8/fileStates.txt").eval(),
                checkStyleParser("build/reports/checkstyle/main.xml").eval("main"),
                checkStyleParser("build/reports/checkstyle/test.xml").eval("test"),
                javacZeroWarnings("build/logs/compile.log"))

        val passing = allResults.fold(true) { a, b -> a && b.second }

        return EvaluatorResult(passing,
                if (passing) project.warningPoints else 0.0,
                if (passing) "No warning / style deductions" else "Warning / style deductions",
                allResults.map { it.first to if (it.second) project.warningPoints else 0.0 })
    }
}
