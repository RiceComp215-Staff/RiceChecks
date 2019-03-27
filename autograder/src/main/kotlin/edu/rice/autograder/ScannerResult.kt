//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

/**
 * The expectation is that every scanner will return one of these, indicating
 * whether full marks were given and how many points were awarded. The assumption
 * is that the caller will specify something about what's being tested and what
 * it's worth, and this will be the result.
 *
 * Note a passing result can either have an empty list of deductions, or can
 * have deductions with zeros in the numeric portion and arbitrary strings elsewhere,
 * to report things like "googleJavaStyle: 35/35 files formatted correctly".
 */
data class ScannerResult(val passes: Boolean, val deductions: List<Pair<String, Double>>)

fun passingScannerResult(happyString: String = "") =
        ScannerResult(true,
                if (happyString == "")
                    emptyList()
                else
                    listOf(happyString to 0.0))
