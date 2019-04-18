/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.GradeCoverage;

@GradeCoverage(project = "Sorting")
public class HeapSort<T extends Comparable<? super T>> implements Sorter<T> {
  // Borrowed from RosettaCode, then modified to fit a standard sorting interface we can test
  // https://rosettacode.org/wiki/Sorting_algorithms/Heapsort#Java

  public HeapSort() {}

  @Override
  public void sortInPlace(T[] a) {
    int count = a.length;

    heapify(a, count);

    int end = count - 1;
    while (end > 0) {
      T tmp = a[end];
      a[end] = a[0];
      a[0] = tmp;
      siftDown(a, 0, end - 1);
      end--;
    }
  }

  private void heapify(T[] a, int count) {
    int start = (count - 2) / 2;

    while (start >= 0) {
      siftDown(a, start, count - 1);
      start--;
    }
  }

  private void siftDown(T[] a, int start, int end) {
    int root = start;

    while ((root * 2 + 1) <= end) {
      int child = root * 2 + 1;
      if (child + 1 <= end && a[child].compareTo(a[child + 1]) < 0) {
        child = child + 1;
      }
      if (a[root].compareTo(a[child]) < 0) {
        T tmp = a[root];
        a[root] = a[child];
        a[child] = tmp;
        root = child;
      } else {
        return;
      }
    }
  }
}
