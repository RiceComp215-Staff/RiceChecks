//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompilerLogScannerTest {
    @Test
    fun testCompilerErrors() {
        val badFiles = listOf("mainfail.log", "testfail.log").map {
            readResource("comp215-build/logs/$it").getOrFail()
        }

        badFiles.forEach { assertFalse(javacZeroWarnings(it).passes) }
    }

    @Test
    fun goodCompilation() {
        assertTrue(javacZeroWarnings(readResource("comp215-build/logs/success.log").getOrFail()).passes)
    }
}