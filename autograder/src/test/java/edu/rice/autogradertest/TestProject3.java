/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autogradertest;

import static edu.rice.autogradertest.Project3.choose;
import static edu.rice.autogradertest.Project3.factorial;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.Arrays;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@GradeProject(
    name = "TP3",
    description = "Factorial and Choose",
    warningPoints = 1.0,
    coveragePoints = 1.0,
    coveragePercentage = 80,
    coverageMethod = "INSTRUCTIONS")
@GradeTopic(project = "TP3", topic = "Correctness")
public class TestProject3 {
  @TestFactory
  @Grade(project = "TP3", topic = "Correctness", points = 2.0, maxPoints = 4.0)
  Iterable<DynamicTest> testFactorial() {
    return Arrays.asList(
        dynamicTest("0", () -> assertEquals(1, factorial(0))),
        dynamicTest("1", () -> assertEquals(1, factorial(1))),
        dynamicTest("3", () -> assertEquals(6, factorial(3))));
  }

  @Test
  @Grade(project = "TP3", topic = "Correctness", points = 4.0)
  void testChoose() {
    assertEquals(15, choose(6, 2));
  }
}
