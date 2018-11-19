package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VerifyTestAnnotations {
    @Test
    fun testDataSanityTest() {
        val result = scanEverything("edu.rice.autogradertest")

        assertEquals(5, result["TP1"]?.topics?.flatMap { it.tests }?.size ?: -1)
        assertEquals(6, result["TP2"]?.topics?.flatMap { it.tests }?.size ?: -1)
        assertEquals(2, result["TP3"]?.topics?.flatMap { it.tests }?.size ?: -1)
        val allGradeTestAnnotations = result.values.flatMap { it.topics }.flatMap { it.tests }
        assertEquals(13, allGradeTestAnnotations.size)
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
}
