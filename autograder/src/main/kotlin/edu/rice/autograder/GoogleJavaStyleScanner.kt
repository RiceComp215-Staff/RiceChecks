//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

/**
 * Given the contents of the output of running the _verifyGoogleJavaFormat_ gradle action (a text file,
 * typically named _fileStates.txt_), returns a [ScannerResult] describing whether every file
 * is properly formatted. See the [plugin sourcecode](https://github.com/sherter/google-java-format-gradle-plugin)
 * for details.
 */
fun googleJavaStyleScanner(data: String, numPoints: Int = 1): ScannerResult {
    // the data is formatted something like this:
    //    src/test/java/edu/rice/week12mockito/Week12LabTest.java,1546873388046247000,4759,FORMATTED
    //    src/main/java/edu/rice/prettypictures/Allele.java,1550594030478707000,18487,FORMATTED
    //    src/main/java/edu/rice/cparser/SExpression.java,1551735069772221000,7236,FORMATTED
    //    src/test/java/edu/rice/qt/QtHelpers.java,1553526776339910000,1884,UNFORMATTED


    // So, that's CSV format with fileName,fileModTime,fileSize,fileState
    // filestate can apparently be FORMATTED, UNFORMATTED, INVALID, or UNKNOWN

    // So, all we have to do is crack the CSV and it's then all-or-nothing. If everything
    // is FORMATTED, then pass, otherwise fail.
}