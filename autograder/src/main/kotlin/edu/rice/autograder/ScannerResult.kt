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
 */
data class ScannerResult(val passes: Boolean, val points: Double)
