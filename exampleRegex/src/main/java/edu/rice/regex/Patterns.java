/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.regex;

import org.intellij.lang.annotations.Language;

public class Patterns {
  // Java class-names start with capital letters and after that, by convention, we use
  // upper and lower-case letters and numbers, and nothing else. Write a regular
  // expression that matches this.
  @Language("RegExp")
  public static final String classPattern = "[A-Z][A-Za-z0-9]*";

  // Java method-names start with a lower-case letter, and after that, by convention,
  // we use upper and lower-case letters and numbers, and nothing else. Write a regular
  // expression that matches this.
  @Language("RegExp")
  public static final String methodPattern = "[a-z][A-Za-z0-9]*";

  // Java base-10 integers can have an optional minus sign at the front, then a series of
  // digits. Note that "negative zero" is allowed, but 0 followed by digits isn't (that's
  // a base-8 "octal" number). Also, an optional capital L at the end signifies a "long"
  // number. Also, underscores may appear between any digits. Write a regular expression
  // that matches this.
  @Language("RegExp")
  public static final String integerPattern = "(-)?(0|[1-9](_[0-9]|[0-9])*)(L)?";
  //  or try this: "(-)?(0|[1-9][0-9]*)(L)?"; -- doesn't know about underscores
}
