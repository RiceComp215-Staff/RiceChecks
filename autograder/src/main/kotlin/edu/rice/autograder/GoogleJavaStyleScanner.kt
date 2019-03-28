//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.getOrElse
import java.util.*

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
        val formattedStatus: String)

/**
 * Given the contents of the output of running the _verifyGoogleJavaFormat_ gradle action (a text file,
 * typically named _fileStates.txt_), returns a [ScannerResult] describing whether every file
 * is properly formatted. See the [plugin sourcecode](https://github.com/sherter/google-java-format-gradle-plugin)
 * for details.
 */
fun googleJavaStyleScanner(data: String, deduction: Double = 1.0): ScannerResult {
    // Using hand-build lame parser because couldn't get Jackson CSV to work
    val results = data
        .split(Regex("[\n\r]+"))
        .filter { it != "" }
        .map { it.split(",") }
        .map { (a, b, c, d) -> GoogleJavaStyleResult(a, b.toLong(), c.toLong(), d) }
        .sortedBy { it.fileName }

    val numResults = results.size
    val numFormatted = results.filter { it.formattedStatus == "FORMATTED" }.size
    val feedback = "googleJavaStyleScanner: %d/%d files correctly formatted".format(numFormatted, numResults)

    return if (numFormatted == numResults) {
        passingScannerResult(feedback);
    } else {
        val badFiles = results
                .filter { it.formattedStatus != "FORMATTED" }
                .map { "-- Incorrect formatting: ${it.fileName}" }
        ScannerResult(false,
                listOf(feedback to deduction) + badFiles.map { it to 0.0 })
    }
}