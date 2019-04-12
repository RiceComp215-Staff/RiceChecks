/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder.annotations;

import java.lang.annotation.*;

/**
 * This annotation, which may appear either on a class or on a package (i.e., in
 * <i>package-info.java</i>), may appear multiple times and specifies a series of "topics",
 * representing groups of items being graded. Every {@link Grade} annotation names a topic.
 *
 * <p>Topics might be broad things like "Correctness", "Performance", etc., or they might be
 * anything else that fits the assignment, e.g., "Easy", "Medium", "Hard".
 *
 * <p>The general idea, then, is that all of the unit tests for a given project and topic, as
 * specified by Grade annotations, will deduct their points, if they fail, from the pool of points
 * specified in the GradeTopic. This means it's possible to have more deductions than available
 * points, but the resulting point score for that topic will never go below zero.
 *
 * <p>If <b>maxPoints</b> isn't specified, it will be derived by adding together all of the {@link
 * Grade} tests associated with the given GradeTopic.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Repeatable(GradeTopics.class)
public @interface GradeTopic {
  /** Name of the project being graded (e.g., "Week1"). */
  String project();

  /** Name of the topic being defined (e.g., "Correctness"). */
  String topic();

  /**
   * Number of points to be associated with the topic. If unspecified, all the individual {@link
   * Grade} annotations for the given project and topic will have their {@link Grade#points()} (or
   * in the case of TestFactory tests, {@link Grade#maxPoints()}) accumulated together.
   */
  double maxPoints() default 0.0;
}
