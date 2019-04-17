/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.GradeCoverage;

@GradeCoverage(project = "Sorting")
public class InsertionSort<T extends Comparable<? super T>> implements Sorter<T> {
  public InsertionSort() {}

  @Override
  public void sortInPlace(T[] a) {
    // Borrowed from RosettaCode, with some changes to make it suitably generic.
    // https://rosettacode.org/wiki/Sorting_algorithms/Insertion_sort#Java
    for (int i = 1; i < a.length; i++) {
      var value = a[i];
      int j = i - 1;
      while (j >= 0 && a[j].compareTo(value) > 0) {
        a[j + 1] = a[j];
        j = j - 1;
      }
      a[j + 1] = value;
    }
  }
}
