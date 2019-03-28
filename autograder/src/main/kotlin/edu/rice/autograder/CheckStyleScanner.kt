//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

//CheckStyle's XML looks like this:
//<?xml version="1.0" encoding="UTF-8"?>
//<checkstyle version="8.17">
//<file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/ListTheories.java">
//</file>
//<file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/QtHelpers.java">
//<error line="33" severity="warning" message="Line is longer than 140 characters (found 224)." source="com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck"/>
//</file>
//<file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/SequenceGenerators.java">
//</file>
// ...
//</checkstyle>

// So, what we're looking for are any files that have <error> entities inside. If we see
// any of them, then we indicate a failure.

data class CheckStyle(val files: List<File>)
data class File(val errors: List<Error>)
data class Error(val line: String, val severity: String, val message: String, val source: String)
