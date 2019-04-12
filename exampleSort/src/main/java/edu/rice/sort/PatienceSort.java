package edu.rice.sort;

import edu.rice.autograder.annotations.GradeCoverage;
import java.util.*;

@GradeCoverage(project = "Sorting")
public class PatienceSort<T extends Comparable<? super T>> implements Sorter<T> {
  public PatienceSort() {}

  @Override
  public void sortInPlace(T[] n) {
    List<Pile<T>> piles = new ArrayList<>();
    for (var x : n) {
      Pile<T> newPile = new Pile<T>();
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

    PriorityQueue<Pile<T>> heap = new PriorityQueue<Pile<T>>(piles);
    for (int c = 0; c < n.length; c++) {
      Pile<T> smallPile = heap.poll();
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
