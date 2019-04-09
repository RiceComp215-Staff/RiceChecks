//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// These are "disabled" because we just want to be able to run them manually
// rather than as part of a test suite.
class ExerciseProject4 {
    @Test
    @Disabled
    fun runGrader() {
        AutoGrader.autoGrade(arrayOf("--package", "edu.rice.autogradertest",
                "--project", "TP4",
                "--log", "all",
                "--build-dir", "autograder/build",
                "grade"))
    }

    @Test
    @Disabled
    fun debugDump() {
        AutoGrader.autoGrade(arrayOf("--package", "edu.rice.autogradertest",
                "--project", "TP4",
                "debugAnnotations"))
    }
}
