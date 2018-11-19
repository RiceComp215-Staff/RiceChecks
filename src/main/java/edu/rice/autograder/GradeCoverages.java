package edu.rice.autograder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Documented
public @interface GradeCoverages {
  /**
   * When multiple GradeCoverage annotations specified, this is what you get back.
   */
  GradeCoverage[] value();
}

