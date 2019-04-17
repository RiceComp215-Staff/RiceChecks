/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeTopic;
import org.junit.jupiter.api.Test;

@GradeTopic(project = "Sorting", topic = "PatienceSort")
public class PatienceSortTest {
  @Test
  @Grade(project = "Sorting", topic = "PatienceSort", points = 1.0)
  public void cycleSortStrings() {
    TestAnySorter.exerciseStrings(new PatienceSort<>());
  }

  @Test
  @Grade(project = "Sorting", topic = "PatienceSort", points = 1.0)
  public void cycleSortIntegers() {
    TestAnySorter.exerciseIntegers(new PatienceSort<>());
  }
}
