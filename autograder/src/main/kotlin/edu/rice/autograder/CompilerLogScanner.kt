//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

private const val TAG = "JavacWarnings"

val javacLogMissing = "Can't find compiler output" to false

// This is almost too easy: our Gradle configuration copies stdout from the compiler
// to a file: build/logs/compile.log

// If the file has zero length, then the compile succeeded. Anything else, then
// there were warnings and/or errors.

fun javacZeroWarnings(fileData: String): Pair<String, Boolean> {
    val result = if (fileData.isEmpty()) "No compiler warnings or errors" to true
    else "One or more compiler warnings / errors" to false

    Log.i(TAG, "JavaC: $result")
    return result
}
