//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

/**
 * the data is formatted something like this:
 * - src/test/java/edu/rice/week12mockito/Week12LabTest.java,1546873388046247000,4759,FORMATTED
 * - src/main/java/edu/rice/prettypictures/Allele.java,1550594030478707000,18487,FORMATTED
 * - src/main/java/edu/rice/cparser/SExpression.java,1551735069772221000,7236,FORMATTED
 * - src/test/java/edu/rice/qt/QtHelpers.java,1553526776339910000,1884,UNFORMATTED
 * So, that's CSV format with fileName,fileModTime,fileNumBytes,formattedStatus
 * - filestate can apparently be FORMATTED, UNFORMATTED, INVALID, or UNKNOWN
 */
data class GoogleJavaStyleResult(
    val fileName: String,
    val fileModTime: Long,
    val fileNumBytes: Long,
    val formattedStatus: String
)

fun googleJavaStyleEvaluator(results: List<GoogleJavaStyleResult>, deduction: Double = 1.0): EvaluatorResult {
    val numResults = results.size
    val numFormatted = results.filter { it.formattedStatus == "FORMATTED" }.size
    val feedback = "googleJavaStyleScanner: %d/%d files correctly formatted".format(numFormatted, numResults)

    return if (numFormatted == numResults) {
        passingEvaluatorResult(feedback)
    } else {
        val badFiles = results
            .filter { it.formattedStatus != "FORMATTED" }
            .map { "-- Incorrect formatting: ${it.fileName}" }
        EvaluatorResult(false,
            listOf(feedback to deduction) + badFiles.map { it to 0.0 })
    }
}

fun googleJavaStyleParser(fileData: String): List<GoogleJavaStyleResult> =
    // Using hand-build lame parser because Jackson CSV wasn't working and this is easy.
    // Unlikely we'll have escaped commas or other such landmines that would break this.
    fileData
        .split(Regex("[\n\r]+"))
        .filter { it != "" }
        .map { it.split(",") }
        .map { (a, b, c, d) -> GoogleJavaStyleResult(a, b.toLong(), c.toLong(), d) }
        .sortedBy { it.fileName }
