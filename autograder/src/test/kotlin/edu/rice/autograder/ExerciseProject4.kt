//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Test

class ExerciseProject4 {
    @Test
    fun runEverything() {
        AutoGrader.autoGrade(arrayOf("--package", "edu.rice.autogradertest", "--project", "TP4", "--log", "all", "grade"))
    }
}
