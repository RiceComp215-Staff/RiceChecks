/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Sorting", topic = "InsertionSort")
public class InsertionSortTest {
  @Test
  @Grade(project = "Sorting", topic = "InsertionSort", points = 1.0)
  public void insertionSortStrings() {
    TestAnySorter.exerciseStrings(new InsertionSort<>());
  }

  @Test
  @Grade(project = "Sorting", topic = "InsertionSort", points = 1.0)
  public void insertionSortIntegers() {
    TestAnySorter.exerciseIntegers(new InsertionSort<>());
  }
}
