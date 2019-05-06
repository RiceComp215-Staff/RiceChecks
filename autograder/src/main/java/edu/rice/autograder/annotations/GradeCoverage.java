/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotate a specific Java class or package as being subject to a code
 * coverage requirement for a given project. When multiple annotations apply to a given class, they
 * are evaluated from the outside to the inside (i.e., first the package, then the outer class, then
 * the inner class). Whichever annotation is "closest" to a given class will determine whether it is
 * included or excluded from coverage test requirements.
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
