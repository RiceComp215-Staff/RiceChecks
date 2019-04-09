//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.davidmoten.text.utils.WordWrap

/** Thin wrapper around the WordWrap library by David Moten. */
fun wordWrap(text: String, lineWidth: Int): List<String> {
    val flowedText = WordWrap
            .from(text)
            .maxWidth(lineWidth)
            .extraWordChars("0123456789")
            .insertHyphens(true)
            .wrap() ?: ""
    return flowedText.split("\n")
}
