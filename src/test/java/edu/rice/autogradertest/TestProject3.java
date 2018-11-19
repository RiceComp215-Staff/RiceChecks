//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autogradertest;

import edu.rice.autograder.Grade;
import edu.rice.autograder.GradeProject;
import edu.rice.autograder.GradeTopic;
import org.junit.jupiter.api.Test;

import static edu.rice.autogradertest.Project3.choose;
import static edu.rice.autogradertest.Project3.factorial;
import static org.junit.jupiter.api.Assertions.assertEquals;

@GradeProject(
    name = "TP3",
    description = "Factorial and Choose",
    warningPoints = 1.0,
    coveragePoints = 1.0,
    coverageMethod = "INSTRUCTIONS",
    coverageRatio = 0.9)
@GradeTopic(
    project = "TP3",
    topic = "Correctness")
public class TestProject3 {
  @Test
  @Grade(project = "TP3", topic = "Correctness", points = 4.0)
  public void testFactorial() {
    assertEquals(1, factorial(0));
    assertEquals(1, factorial(1));
    assertEquals(6, factorial(3));
  }

  @Test
  @Grade(project = "TP3", topic = "Correctness", points = 4.0)
  public void testChoose() {
    assertEquals(15, choose(6,2));
  }
}
