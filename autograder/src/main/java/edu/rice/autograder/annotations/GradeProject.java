//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder.annotations;

import java.lang.annotation.*;

/**
 * This annotation is the top-level annotation for a programming "project". It specifies the name of
 * the project, an optional <b>description</b>, as well as an optional <b>maxPoints</b>. If missing,
 * the maximum number of points will be computed from all of the specified {@link Grade} annotations
 * for the project. Otherwise, it's entirely possible to have more deductions available from Grade
 * annotations than points present. The ultimate grade will never go below zero.
 *
 * <p>Also possible to specify here is a requirement that there be <b>zeroWarnings</b>. The
 * autograder will look at whether the Java compiler (and ErrorProne, if it's configured to run
 * alongside it) produced any warnings. It will also look at CheckStyle to see if it produced any
 * warnings. If zeroWarnings is true, then the number of points to be deducted is specified by
 * <b>warningPoints</b>.
 *
 * <p>To specify a coverage requirement, specify a non-zero <b>coveragePoints</b>, and optionally
 * specify the <b>coverageStyle</b> and <b>coveragePercentage</b>. Then, annotate source classes on
 * which you want coverage to be measured using {@link GradeCoverage}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Repeatable(GradeProjects.class)
@Documented
public @interface GradeProject {
  /** Specifies the name of the project being graded (e.g., "Week1"). */
  String name();

  /**
   * Specifies an optional description of the project which will appear in the autograder report.
   */
  String description() default "";

  /**
   * Specifies the maximum number of points to be associated with this project. If ignored, it will
   * be computed from all the different Grade annotations associated with the project.
   */
  double maxPoints() default 0.0;

  /**
   * Specifies whether zero warnings are required from CheckStyle, the Java compiler, and (if it's
   * configured) ErrorProne. If any warnings appear, then the points specific here will deducted.
   * Defaults to zero.
   */
  double warningPoints() default 0.0;

  /** If JaCoCo code coverage is required for this project, specify a non-zero point value here. */
  double coveragePoints() default 0.0;

  /** Specifies a JaCoCo coverage ratio as a percentage (e.g., 70) */
  int coveragePercentage() default 0;

  /** Specifies a JaCoCo coverage testing method (currently supports "LINES" or "INSTRUCTIONS"). */
  String coverageMethod() default "LINES";
}
