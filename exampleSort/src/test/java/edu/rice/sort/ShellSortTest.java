/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Sorting", topic = "ShellSort")
public class ShellSortTest {
  @Test
  @Grade(project = "Sorting", topic = "ShellSort", points = 1.0)
  public void shellSortStrings() {
    TestAnySorter.exerciseStrings(new ShellSort<>());
  }

  @Test
  @Grade(project = "Sorting", topic = "ShellSort", points = 1.0)
  public void shellSortIntegers() {
    TestAnySorter.exerciseIntegers(new ShellSort<>());
  }
}
