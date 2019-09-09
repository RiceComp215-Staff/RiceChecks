/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

private const val TAG = "Javac"

val javacLogMissing = CodeStyleDeduction(
    "Compiler: Can't find output",
    0.0, TAG, "", false, 0, 0
)

// This is almost too easy: our Gradle configuration copies stdout from the compiler
// to a file: build/logs/compile.log

// If the file has zero length or has nothing but blank lines, then the compile succeeded.
// Anything else, then there were warnings and/or errors.

fun javacZeroWarnings(fileData: String): CodeStyleDeduction {
    val lines = fileData.split("\n", "\r").filter { it.isNotEmpty() }

    val result =
        if (fileData.isEmpty() || lines.isEmpty()) {
            CodeStyleDeduction(
                "Compiler: No warnings or errors",
                0.0, TAG, "", true, 0, 0
            )
        } else {
            CodeStyleDeduction(
                "Compiler: One or more warnings / errors",
                0.0, TAG, "", false, 0, 0
            )
        }

    Log.i(TAG, result)
    return result
}
