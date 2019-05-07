/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import org.junit.jupiter.api.Test
import edu.rice.autograder.JacocoCounterType.INSTRUCTION
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

class JacocoScannerTest {
    @Test
    fun testLoader() {
        val fileContents =
            readResource("comp215-build/reports/jacoco/test/jacocoTestReport.xml")
                .getOrFail()
        val xmlResults = jacocoParser(fileContents)
        if (xmlResults == null) {
            fail()
        } else {
            val instructionsCovered = xmlResults.counterMap[INSTRUCTION]?.covered ?: 0
            val instructionsMissed = xmlResults.counterMap[INSTRUCTION]?.missed ?: 0

            assertNotEquals(0, instructionsCovered)
            assertNotEquals(0, instructionsMissed)

            val packages = xmlResults.packages ?: emptyList()
            val packageInstructionsCovered = packages.sumBy {
                it.counterMap[INSTRUCTION]?.covered ?: 0
            }
            val packageInstructionsMissed = packages.sumBy {
                it.counterMap[INSTRUCTION]?.missed ?: 0
            }

            assertEquals(instructionsCovered, packageInstructionsCovered)
            assertEquals(instructionsMissed, packageInstructionsMissed)

            val classes = packages.flatMap { it.classes ?: emptyList() }
            val classesInstructionsCovered = classes.sumBy {
                it.counterMap[INSTRUCTION]?.covered ?: 0
            }
            val classesInstructionsMissed = classes.sumBy {
                it.counterMap[INSTRUCTION]?.missed ?: 0
            }

            assertEquals(instructionsCovered, classesInstructionsCovered)
            assertEquals(instructionsMissed, classesInstructionsMissed)
        }
    }

    @Test
    fun testMatchingClassSpecs() {
        val fileContents =
            readResource("comp215-build/reports/jacoco/test/jacocoTestReport.xml")
                .getOrFail()
        val xmlResults = jacocoParser(fileContents)
        if (xmlResults == null) {
            fail()
        } else {
            // we're setting up a non-trivial set of nested inclusion/exclusion directives to
            // see whether our matching logic works correctly
            val coverages = listOf(
                    GGradeCoverage(GCoverageScope.PACKAGE, false,
                        "edu.rice.week2lists"),
                    GGradeCoverage(GCoverageScope.CLASS, true,
                        "edu.rice.week2lists.ObjectList"),
                    GGradeCoverage(GCoverageScope.CLASS, false,
                        "edu.rice.week2lists.ObjectList.Empty"))

            val matchingClasses = xmlResults.matchingClassSpecs(coverages)

            assertTrue(matchingClasses.isNotEmpty())
            assertFalse(matchingClasses.contains("edu.rice.week2lists.ObjectList"))
            assertFalse(matchingClasses.contains("edu.rice.week2lists.ObjectList.Cons"))
            assertTrue(matchingClasses.contains("edu.rice.week2lists.ObjectList.Empty"))
            assertTrue(matchingClasses.contains("edu.rice.week2lists.MList"))
        }
    }

    @Test
    fun anonInnerClassRegexWorks() {
        assertFalse("edu/rice/rpn/RpnCalc".isAnonymousInnerClass())
        assertFalse("edu/rice/rpn/RpnCalc\$1x".isAnonymousInnerClass())
        assertFalse("edu/rice/rpn/RpnCalc\$StackVisitor".isAnonymousInnerClass())
        assertTrue("edu/rice/rpn/RpnCalc\$1".isAnonymousInnerClass())

        // nested anonymous inner classes should look like this
        assertTrue("edu/rice/rpn/RpnCalc\$1\$1".isAnonymousInnerClass())
        assertTrue("edu/rice/rpn/RpnCalc\$1\$23\$1".isAnonymousInnerClass())
        assertTrue("edu/rice/rpn/RpnCalc\$1\$StackVisitor\$1".isAnonymousInnerClass())
        assertFalse("edu/rice/rpn/RpnCalc\$1\$StackVisitor".isAnonymousInnerClass())
    }

    @Test
    fun anonInnerClassOfWorks() {
        assertFalse("edu/rice/rpn/RpnCalc\$StackVisitor"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc")
        assertFalse("edu/rice/rpn/RpnCalc\$StackVisitor"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc\$1")
        assertFalse("edu/rice/rpn/RpnCalc"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc\$1")
        assertFalse("edu/rice/rpn/RpnCalc\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc\$1")
        assertFalse("edu/rice/rpn/RpnCalc\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc\$StackVisitor")
        assertFalse("edu/rice/rpn/RpnCalc\$StackVisitor\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc")
        assertFalse("edu/rice/rpn/RpnCalc\$1\$StackVisitor\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc")

        assertTrue("edu/rice/rpn/RpnCalc\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc")
        assertTrue("edu/rice/rpn/RpnCalc\$1\$2\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc")
        assertTrue("edu/rice/rpn/RpnCalc\$1\$StackVisitor\$1"
            isAnonymousInnerClassOf "edu/rice/rpn/RpnCalc\$1\$StackVisitor")
    }
}
