/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to annotate a specific Java class or package as being subject to a code
 * coverage requirement for a given project. Exclusions are subtracted from inclusions. In normal
 * usage, you might include a top-level class and then, perhaps, exclude an inner class. This will
 * work how you expect. If you try the opposite (exclude the outer, include the inner), you'll get
 * nothing at all.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Repeatable(GradeCoverages.class)
@Documented
public @interface GradeCoverage {
  /** The <b>project</b> is specified the same as in {@link Grade#project()}. */
  String project();

  /**
   * Sometimes you want to <i>exclude</i> a class or package from consideration for coverage
   * testing.
   */
  boolean exclude() default false;
}
