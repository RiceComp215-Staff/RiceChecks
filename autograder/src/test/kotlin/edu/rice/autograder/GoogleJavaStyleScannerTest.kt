//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoogleJavaStyleScannerTest {
    @Test
    fun testReadDir() {
        val comp215BuildDir = readResourceDir("comp215-build").getOrFail().toList()
        assertEquals(4, comp215BuildDir.size)
        assertTrue(comp215BuildDir.contains("comp215-build/google-java-format"))
    }

    @Test
    fun testFileStatesExample() {
        val input = readResource("comp215-build/google-java-format/0.8/fileStates.txt").getOrFail()
        val testResult = googleJavaStyleEvaluator(googleJavaStyleParser(input), 1.0)

        assertFalse(testResult.passes)
        assertEquals(2, testResult.deductions.size)
        assertEquals(1.0, testResult.deductions[0].second)
        assertEquals(0.0, testResult.deductions[1].second)
        assertTrue(testResult.deductions[1].first.contains("src/test/java/edu/rice/qt/QtHelpers.java"))
    }
}