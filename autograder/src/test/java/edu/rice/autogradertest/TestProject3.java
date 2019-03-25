//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autogradertest;

import edu.rice.autograder.Grade;
import edu.rice.autograder.GradeProject;
import edu.rice.autograder.GradeTopic;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static edu.rice.autogradertest.Project3.choose;
import static edu.rice.autogradertest.Project3.factorial;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@GradeProject(
    name = "TP3",
    description = "Factorial and Choose",
    warningPoints = 1.0,
    coveragePoints = 1.0,
//    coverageRatio =   0.64885,
//    coverageRatio = 0.6489, // -- or larger -- leads to a NaN when reading this, but why?
    coveragePercentage = 80,
    coverageMethod = "INSTRUCTIONS"
)
@GradeTopic(
    project = "TP3",
    topic = "Correctness")
public class TestProject3 {
  @TestFactory
  @Grade(project = "TP3", topic = "Correctness", points = 2.0, maxPoints = 4.0)
  public Iterable<DynamicTest> testFactorial() {
    return List.of(
        dynamicTest("0", () -> assertEquals(1, factorial(0))),
        dynamicTest("1", () -> assertEquals(1, factorial(1))),
        dynamicTest("3", () -> assertEquals(6, factorial(3))));
  }

  @Test
  @Grade(project = "TP3", topic = "Correctness", points = 4.0)
  public void testChoose() {
    assertEquals(15, choose(6,2));
  }
}
