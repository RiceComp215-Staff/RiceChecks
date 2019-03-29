//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class JUnitSuiteScannerTest {
    @Test
    fun testLoader() {
        val files = readResourceDir("comp215-build/test-results/test").fold({ emptyList<String>() }, { it.toList() })
        val fileContents = files.flatMap { readResource(it).asList() }
        val xmlResults = fileContents.map { junitSuiteParser(it) }
        val allTests = xmlResults.flatMap { it.tests ?: emptyList() }
        val failedTests = allTests.filter { it.failure != null }

        assertEquals(1287, xmlResults.sumBy { it.numTests })
        assertEquals(1287, allTests.size)
        assertEquals(10, xmlResults.sumBy { it.numFailures })
        assertEquals(10, failedTests.size)
        assertEquals(0, xmlResults.sumBy { it.numSkipped })

        val stringConcatResult = failedTests.filter { it.className == "edu.rice.qt.ListTheories" }
        assertEquals(2, stringConcatResult.size)

        val stackTrace = stringConcatResult[0].failure?.stackTraceList ?: fail()
        assertEquals("org.opentest4j.AssertionFailedError", stackTrace[0])
        assertEquals("at edu.rice.qt.ListTheories.stringConcatenationIsNotCommutative(ListTheories.java:132)", stackTrace[3])
    }
}
