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

class CheckStyleResultsScannerTest {
    @Test
    fun testCheckStyleInputs() {
        val mainData = readResource("comp215-build/reports/checkstyle/main.xml").getOrFail()
        val mainResults = checkStyleParser(mainData).eval("main")
        val testData = readResource("comp215-build/reports/checkstyle/test.xml").getOrFail()
        val testResults = checkStyleParser(testData).eval("test")

        assertTrue(mainResults.second) // passing
        assertFalse(testResults.second) // failing!
    }
}