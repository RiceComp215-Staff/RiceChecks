/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import edu.rice.autograder.annotations.GradeCoverage;
import java.util.*;

@GradeCoverage(project = "Sorting")
public class PatienceSort<T extends Comparable<? super T>> implements Sorter<T> {
  public PatienceSort() {}

  @Override
  public void sortInPlace(T[] n) {
    // Started with RosettaCode, made more generic.
    // https://rosettacode.org/wiki/Sorting_algorithms/Patience_sort#Java

    var piles = new ArrayList<Pile<T>>();
    for (var x : n) {
      var newPile = new Pile<T>();
      newPile.push(x);
      int i = Collections.binarySearch(piles, newPile);
      if (i < 0) {
        i = ~i;
      }
      if (i != piles.size()) {
        piles.get(i).push(x);
      } else {
        piles.add(newPile);
      }
    }

    var heap = new PriorityQueue<>(piles);
    for (int c = 0; c < n.length; c++) {
      var smallPile = heap.poll();
      n[c] = smallPile.pop();
      if (!smallPile.isEmpty()) {
        heap.offer(smallPile);
      }
    }
    assert (heap.isEmpty());
  }

  private static class Pile<T extends Comparable<? super T>> extends ArrayDeque<T>
      implements Comparable<Pile<T>> {
    @Override
    public int compareTo(Pile<T> y) {
      return peek().compareTo(y.peek());
    }
  }
}
