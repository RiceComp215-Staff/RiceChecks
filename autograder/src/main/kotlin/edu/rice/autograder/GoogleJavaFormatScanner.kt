/*
 * AnnoAutoGrader
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

private const val TAG = "GoogleJavaFormat"

/**
 * the data is formatted something like this:
 * - src/test/java/edu/rice/week12mockito/Week12LabTest.java,1546873388046247000,4759,FORMATTED
 * - src/main/java/edu/rice/prettypictures/Allele.java,1550594030478707000,18487,FORMATTED
 * - src/main/java/edu/rice/cparser/SExpression.java,1551735069772221000,7236,FORMATTED
 * - src/test/java/edu/rice/qt/QtHelpers.java,1553526776339910000,1884,UNFORMATTED
 * So, that's CSV format with fileName,fileModTime,fileNumBytes,formattedStatus
 * - filestate can apparently be FORMATTED, UNFORMATTED, INVALID, or UNKNOWN
 */
data class GoogleJavaFormatResult(
    val fileName: String,
    val fileModTime: Long,
    val fileNumBytes: Long,
    val formattedStatus: String
)

val googleJavaFormatMissing = "$TAG: no input found" to false

fun List<GoogleJavaFormatResult>.eval(): Pair<String, Boolean> {
    if (isEmpty()) {
        return googleJavaFormatMissing
    }

    val numResults = size
    val numFormatted = filter { it.formattedStatus == "FORMATTED" }.size
    val feedback = "$TAG: %d of %d files passed".format(numFormatted, numResults) +
            if (numFormatted == numResults)
                ""
            else
                "; run the <googleJavaFormat> gradle action to fix"
    Log.i(TAG, "eval: $feedback")

    return feedback to (numFormatted == numResults)
}

fun googleJavaFormatParser(fileData: String): List<GoogleJavaFormatResult> {
    Log.i(TAG, "parser: ${fileData.length} bytes")

    return if (fileData.isEmpty()) {
        emptyList()
    } else {
        // Using hand-build lame parser because Jackson CSV wasn't working and this is easy.
        // Unlikely we'll have escaped commas or other such landmines that would break this.
        fileData
                .split(Regex("[\n\r]+"))
                .filter { it != "" }
                .map { it.split(",") }
                .map { (a, b, c, d) ->
                    GoogleJavaFormatResult(a, b.toLong(), c.toLong(), d) }
                .sortedBy { it.fileName }
    }
}
