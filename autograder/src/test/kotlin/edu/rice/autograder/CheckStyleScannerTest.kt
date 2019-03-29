//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CheckStyleScannerTest {
    @Test
    fun testCheckStyleInputs() {
        val mainData = readResource("comp215-build/reports/checkstyle/main.xml").fold({ "" }, { it })
        val mainResults = checkStyleScanner("main", mainData, 1.0)
        val testData = readResource("comp215-build/reports/checkstyle/test.xml").fold({ "" }, { it })
        val testResults = checkStyleScanner("test", testData, 1.0)

        assertTrue(mainResults.passes)
        assertEquals(0.0, mainResults.deductions[0].second)
        assertFalse(testResults.passes)
        assertEquals(1.0, testResults.deductions[0].second)
    }
}