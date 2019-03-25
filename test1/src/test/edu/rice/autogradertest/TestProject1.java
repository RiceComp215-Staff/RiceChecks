//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autogradertest;

import edu.rice.autograder.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@GradeProject(name = "TP1",
    description = "A fairly basic project",
    maxPoints = 10.0,
    coverageRatio = 0.5,
    coveragePoints = 1.0,
    coverageMethod = "INSTRUCTIONS",
    warningPoints = 1.0)
@GradeTopic(project = "TP1",
    topic = "Group1",
    maxPoints = 5.0)
@GradeTopic(project = "TP1",
    topic = "Group2",
    maxPoints = 4.0)
public class TestProject1 {
  @Grade(project = "TP1", topic = "Group1", points = 1.0)
  @Test
  public void test1() {
    assertTrue(true);
  }

  @Grade(project = "TP1", topic = "Group1", points = 1.0)
  @Test
  public void test2() {
    assertTrue(true);
  }

  @Grade(project = "TP1", topic = "Group1", points = 1.0)
  @Test
  public void test3() {
    assertTrue(true);
  }

  @Grade(project = "TP1", topic = "Group2", points = 1.0)
  @Test
  public void test4() {
    assertTrue(true);
  }

  @Grade(project = "TP1", topic = "Group2", points = 1.0)
  @Grade(project = "TP2", topic = "Group2", points = 1.0)
  @Test
  public void test5() {
    assertTrue(true);
  }
}
