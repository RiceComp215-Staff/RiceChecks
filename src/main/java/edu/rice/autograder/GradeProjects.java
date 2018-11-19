package edu.rice.autograder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Documented
public @interface GradeProjects {
  /**
   * When multiple GradeProject annotations are specified, this is what you get back.
   */
  GradeProject[] value();
}
