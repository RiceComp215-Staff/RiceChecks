//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GoogleJavaStyleScannerTest {
    @Test
    fun testFileStatesExample() {
        val input = ClassLoader
                .getSystemResourceAsStream("comp215-build/google-java_format.0.8/fileStates.txt")
                .readBytes()
                .contentToString()

        val testResult = googleJavaStyleScanner(input, 1.0)

        assertFalse(testResult.passes)
        assertEquals(2, testResult.deductions.size)
        assertEquals(1.0, testResult.deductions[0].second)
        assertEquals(0.0, testResult.deductions[1].second)
        assertTrue(testResult.deductions[1].first.contains("src/test/java/edu/rice/qt/QtHelpers.java"))
    }
}