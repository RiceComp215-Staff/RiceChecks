/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Sorting", topic = "HeapSort")
public class HeapSortTest {
  @Test
  @Grade(project = "Sorting", topic = "HeapSort", points = 1.0)
  public void heapSortStrings() {
    TestAnySorter.exerciseStrings(new HeapSort<>());
  }

  @Test
  @Grade(project = "Sorting", topic = "HeapSort", points = 1.0)
  public void heapSortIntegers() {
    TestAnySorter.exerciseIntegers(new HeapSort<>());
  }
}
