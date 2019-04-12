/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autogradertest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rice.autograder.annotations.Grade;
import org.junit.jupiter.api.Test;

public class TestProject2 {
  @Grade(project = "TP2", topic = "Group1", points = 1.0)
  @Test
  public void test1() {
    assertTrue(true);
  }

  @Grade(project = "TP2", topic = "Group1", points = 1.0)
  @Test
  public void test2() {
    assertTrue(true);
  }

  @Grade(project = "TP2", topic = "Group1", points = 1.0)
  @Test
  public void test3() {
    assertTrue(true);
  }

  @Grade(project = "TP2", topic = "Group2", points = 1.0)
  @Test
  public void test4() {
    assertTrue(true);
  }

  @Grade(project = "TP2", topic = "Group2", points = 1.0)
  @Test
  public void test5() {
    assertTrue(true);
  }
}
