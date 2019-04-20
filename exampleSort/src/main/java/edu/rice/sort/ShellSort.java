/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.GradeCoverage;

@GradeCoverage(project = "Sorting")
public class ShellSort<T extends Comparable<? super T>> implements Sorter<T> {
  public ShellSort() {}

  @Override
  public void sortInPlace(T[] a) {
    // Code from RosettaCode, except made generic.
    // https://rosettacode.org/wiki/Sorting_algorithms/Shell_sort#Java

    int increment = a.length / 2;
    while (increment > 0) {
      for (int i = increment; i < a.length; i++) {
        int j = i;
        var temp = a[i];
        while (j >= increment && a[j - increment].compareTo(temp) > 0) {
          a[j] = a[j - increment];
          j = j - increment;
        }
        a[j] = temp;
      }
      if (increment == 2) {
        increment = 1;
      } else {
        increment = (int) (increment * 5.0 / 11);
      }
    }
  }
}
