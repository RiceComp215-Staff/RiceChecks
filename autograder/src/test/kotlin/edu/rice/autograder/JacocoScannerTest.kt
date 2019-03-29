//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import edu.rice.autograder.JacocoCounterType.*

class JacocoScannerTest {
    @Test
    fun testLoader() {
        val fileContents = readResource("comp215-build/reports/jacoco/test/jacocoTestReport.xml").getOrFail()
        val xmlResults = jacocoParser(fileContents)
        val instructionsCovered = xmlResults.counterMap[INSTRUCTION]?.covered ?: 0
        val instructionsMissed = xmlResults.counterMap[INSTRUCTION]?.missed ?: 0

        assertNotEquals(0, instructionsCovered)
        assertNotEquals(0, instructionsMissed)

        val packages = xmlResults.packages ?: emptyList()
        val packageInstructionsCovered = packages.sumBy { it.counterMap[INSTRUCTION]?.covered ?: 0 }
        val packageInstructionsMissed = packages.sumBy { it.counterMap[INSTRUCTION]?.missed ?: 0 }

        assertEquals(instructionsCovered, packageInstructionsCovered)
        assertEquals(instructionsMissed, packageInstructionsMissed)

        val classes = packages.flatMap { it.classes ?: emptyList() }
        val classesInstructionsCovered = classes.sumBy { it.counterMap[INSTRUCTION]?.covered ?: 0 }
        val classesInstructionsMissed = classes.sumBy { it.counterMap[INSTRUCTION]?.missed ?: 0 }

        assertEquals(instructionsCovered, classesInstructionsCovered)
        assertEquals(instructionsMissed, classesInstructionsMissed)
    }
}
