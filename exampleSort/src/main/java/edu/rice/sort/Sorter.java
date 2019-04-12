/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

public interface Sorter<T extends Comparable<? super T>> {
  /** Sorts the given data in its natural ({@link Comparable} order. */
  void sortInPlace(T[] data);

  /** Helper method: checks whether an array is sorted in its natural ({@link Comparable}) order. */
  static <T extends Comparable<? super T>> boolean isSorted(T[] data) {
    T previous = null;

    for (var x : data) {
      if (previous != null && previous.compareTo(x) > 0) {
        return false;
      }
      previous = x;
    }

    return true;
  }
}
