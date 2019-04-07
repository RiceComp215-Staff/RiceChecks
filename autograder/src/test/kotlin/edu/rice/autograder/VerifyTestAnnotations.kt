//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class VerifyTestAnnotations {
    internal val result = scanEverything("edu.rice.autogradertest")

    @Test
    fun testDataSanityTest() {
        assertEquals(5, result["TP1"]?.topics?.flatMap { it.tests }?.size ?: -1)
        assertEquals(6, result["TP2"]?.topics?.flatMap { it.tests }?.size ?: -1)
        assertEquals(2, result["TP3"]?.topics?.flatMap { it.tests }?.size ?: -1)
        val allGradeTestAnnotations = result.values.flatMap { it.topics }.flatMap { it.tests }
        assertEquals(13, allGradeTestAnnotations.size)
    }

    private fun testSaneDouble(s: String, d: Double): DynamicTest = dynamicTest(s) {
        assertTrue(d.isFinite())
        assertTrue(d >= 0.0)
    }

    // We've got code in the scanner that will note if it ever finds conditions like this,
    // but it's handy to still test for it.
    @TestFactory
    fun noNaNsAnywhere(): Iterable<DynamicTest> = result.keys
            .flatMap { key ->
                result[key]?.topics?.flatMap { topic ->
                    val prefixStr = "Key ($key), Topic(${topic.name})"
                    val testMaxPoints = dynamicTest("$prefixStr, MaxPoints") { assertTrue(topic.maxPoints.isFinite()); assertTrue(topic.maxPoints >= 0) }
                    val methodTests = topic.tests.flatMap { test -> listOf(
                            testSaneDouble("$prefixStr, ${test.className} / ${test.methodName}", test.points),
                            testSaneDouble("$prefixStr, ${test.className} / ${test.methodName}", test.points))
                    }
                    methodTests + testMaxPoints
                } ?: emptyList()
            }

    @Test
    fun printTP1() {
        System.out.print(yamlExporter("TP1", "edu.rice.autogradertest"))
    }

    @Test
    fun printTP2() {
        System.out.print(yamlExporter("TP2", "edu.rice.autogradertest"))
    }

    @Test
    fun printTP3() {
        System.out.print(yamlExporter("TP3", "edu.rice.autogradertest"))
    }

    @Test
    fun textExportImportEquality() {
        // This test is really important: so long as we trust that we can export and import our
        // GGradeProject objects, without any loss or weirdness, then we can extract them from
        // the source code and store them in the config directory. This means that students
        // won't accidentally (or maliciously) change the test policy by tweaking the grade
        // annotations in the source code they see. We could even strip the annotations
        // completely out of the reference code prior to distribution to the students.

        result.forEach { (name, project) ->
            assertEquals(project, yamlImporter(project.yamlExporter()).getOrFail(), name)
        }
    }
}
