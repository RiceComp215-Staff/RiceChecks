//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autogradertest;

import org.intellij.lang.annotations.Language;

public class Project4 {
  // Java class-names start with capital letters and after that, by convention, we use
  // upper and lower-case letters and numbers, and nothing else. Write a regular
  // expression that matches this.
  @Language("RegExp")
  public static final String classPattern = "";

  // Java method-names start with a lower-case letter, and after that, by convention,
  // we use upper and lower-case letters and numbers, and nothing else. Write a regular
  // expression that matches this.
  @Language("RegExp")
  public static final String methodPattern = "";

  // Java base-10 integers can have an optional minus sign at the front, then a series of
  // digits. Note that "negative zero" is allowed, but 0 followed by digits isn't (that's
  // a base-8 "octal" number). Also, an optional capital L at the end signifies a "long"
  // number. Also, underscores may appear between any digits. Write a regular expression
  // that matches this.
  @Language("RegExp")
  public static final String integerPattern = "";
}
