package edu.rice.autograder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Grades {
  /**
   * When multiple Grade annotations are applied, this is what you get back.
   */
  Grade[] value();
}
