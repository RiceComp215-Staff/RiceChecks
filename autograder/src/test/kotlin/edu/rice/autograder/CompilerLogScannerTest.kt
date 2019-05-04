/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

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

        badFiles.forEach { assertFalse(javacZeroWarnings(it).passing) }
    }

    @Test
    fun goodCompilation() {
        assertTrue(javacZeroWarnings(
            readResource("comp215-build/logs/success.log").getOrFail()).passing)
    }

    @Test
    fun goodCompilationWhitespace() {
        assertTrue(javacZeroWarnings(
            readResource("comp215-build/logs/whitespace-success.log").getOrFail()).passing)
    }
}
