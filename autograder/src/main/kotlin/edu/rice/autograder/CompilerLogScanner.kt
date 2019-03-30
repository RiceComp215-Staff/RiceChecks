//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

// This is almost too easy: our Gradle configuration copies stdout from the compiler
// to a file: build/logs/compile.log

// If the file has zero length, then the compile succeeded. Anything else, then
// there were warnings and/or errors.

fun javacZeroWarnings(fileData: String, deduction: Double = 1.0): EvaluatorResult =
    if (fileData.length == 0) passingEvaluatorResult("No compiler warnings or errors")
    else EvaluatorResult(false, listOf("One or more compiler warnings / errors" to deduction))
