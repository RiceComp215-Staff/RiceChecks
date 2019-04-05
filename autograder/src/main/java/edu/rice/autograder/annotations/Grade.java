//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to annotate a specific unit test to indicate which project it might be
 * (e.g., "Week1") and what it's topic might be (e.g., "Correctness"). Also specified should be the
 * number of points that the unit test is worth.
 *
 * <p>A Grade annotation should be placed on a JUnit5 unit test, built using the new "Jupiter" APIs
 * (org.junit.jupiter.api.Test or org.junit.jupiter.api.TestFactory). Anything else is considered an
 * error.
 *
 * <p>Also of note: multiple Grade annotations on a given unit test are acceptable. Each annotation
 * should specify a different project. (Example: if you've got a set of unit tests that will be used
 * across multiple weeks of assignments, and you want them to be used in each week's project, then
 * simply have multiple Grade annotations.)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Repeatable(Grades.class)
public @interface Grade {
  /** Name of the project being graded (e.g., "Week1"). */
  String project();

  /** Name of the topic within the project being graded (e.g., "Correctness"). */
  String topic();

  /**
   * Number of points associated with the unit test being annotated.
   *
   * <p>When applied to a JUnit5 Test, the <b>points</b> are then subtracted from the requested
   * topic if the test fails. If more points are subtracted than a given topic has to begin with,
   * then scores won't go below zero. <b>maxPoints</b> is ignored.
   *
   * <p>When applied to a JUnit5 TestFactory, which returns a list of dynamic tests to be executed,
   * each individual test will be worth the requested number of <b>points</b>, but the maximum
   * deduction for failed tests from the given test factory will be <b>maxPoints</b>.
   */
  double points();

  /**
   * When applied to a JUnit5 TestFactory, describes the maximum number of points to be associated
   * with the TestFactory. This field is ignored for regular unit tests.
   */
  double maxPoints() default 0.0;
}
