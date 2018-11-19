package edu.rice.autograder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Documented
public @interface GradeTopics {
  /**
   * When multiple GradeTopic annotations are specified, this is what you get back.
   */
  GradeTopic[] value();
}
