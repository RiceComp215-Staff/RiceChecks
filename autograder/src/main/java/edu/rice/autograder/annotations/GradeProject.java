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
 * This annotation is the top-level annotation for a programming "project". It specifies the name of
 * the project, an optional <b>description</b>, as well as an optional <b>maxPoints</b>. If missing,
 * the maximum number of points will be computed from all of the specified {@link Grade} annotations
 * for the project. Otherwise, it's entirely possible to have more deductions available from Grade
 * annotations than points present. The ultimate grade will never go below zero.
 *
 * <p>It's also possible to specify here a requirement that there be <b>zeroWarnings</b>. The
 * autograder will look at whether the Java compiler (and ErrorProne, if it's configured to run
 * alongside it) produced any warnings. By default, it also considers CheckStyle and
 * google-java-format. If zeroWarnings is true, then the number of points to be deducted is
 * specified by <b>warningPoints</b>. Each of these individual checks can be disabled with optional
 * parameters like <b>useCheckStyle</b> or <b>useGoogleJavaFormat</b>.
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
   * Specifies whether zero warnings are required from CheckStyle and so forth. If any warnings
   * appear, then the points specific here will deducted. Defaults to zero.
   */
  double warningPoints() default 0.0;

  /** Specifies whether CheckStyle is considered as part of the warningPoints. Defaults to true. */
  boolean useCheckStyle() default true;

  /**
   * Specifies whether GoogleJavaFormat is considered as part of the warningPoints. Defaults to
   * true.
   */
  boolean useGoogleJavaFormat() default true;

  /**
   * Specifies whether Java compiler warnings are considered as part of the warningPoints. Defaults
   * to true. (If you've enabled ErrorProne, its warnings are included alongside the Java compiler's
   * warnings.)
   */
  boolean useJavacWarnings() default true;

  /** If JaCoCo code coverage is required for this project, specify a non-zero point value here. */
  double coveragePoints() default 0.0;

  /** Specifies a JaCoCo coverage ratio as a percentage (e.g., 70) */
  int coveragePercentage() default 0;

  /** Specifies a JaCoCo coverage testing method (currently supports "LINES" or "INSTRUCTIONS"). */
  String coverageMethod() default "LINES";
}
