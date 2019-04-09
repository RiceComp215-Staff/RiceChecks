//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WordWrapTest {
    @Test
    fun basics() {
        assertEquals(listOf("Hello", "World"), wordWrap("Hello World", 6))
    }

    @Test
    fun lessBasics() {
        assertEquals(listOf("edu.rice.autogradertest.TestProject4.",
                "testClassIdentifiers: missing"),
                wordWrap("edu.rice.autogradertest.TestProject4.testClassIdentifiers: missing", 40))
        assertEquals(listOf("edu.rice.autogradertest.",
                "TestProject4.",
                "testClassIdentifiers: missing"),
                wordWrap("edu.rice.autogradertest.TestProject4.testClassIdentifiers: missing", 30))
    }
}
