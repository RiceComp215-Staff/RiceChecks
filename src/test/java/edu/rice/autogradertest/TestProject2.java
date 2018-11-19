package edu.rice.autogradertest;

import edu.rice.autograder.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
