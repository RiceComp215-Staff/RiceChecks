//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.Option
import arrow.core.getOrElse
import arrow.data.extensions.sequence.foldable.size
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import edu.rice.autograder.*

class GoogleJavaStyleScannerTest {
    @Test
    fun testReadDir() {
        val comp215BuildDir = readResourceDir("comp215-build")
        assertTrue(comp215BuildDir.isSuccess())

        val seq = comp215BuildDir.fold({ emptyList<String>() }) { it.toList() }
        assertEquals(3, seq.size)
        assertTrue(seq.contains("comp215-build/google-java-format"))
    }

    @Test
    fun testFileStatesExample() {
        val input = readResource("comp215-build/google-java-format/0.8/fileStates.txt")
            .fold({ "" }, { it })

        val testResult = googleJavaStyleScanner(input, 1.0)

        assertFalse(testResult.passes)
        assertEquals(2, testResult.deductions.size)
        assertEquals(1.0, testResult.deductions[0].second)
        assertEquals(0.0, testResult.deductions[1].second)
        assertTrue(testResult.deductions[1].first.contains("src/test/java/edu/rice/qt/QtHelpers.java"))
    }
}