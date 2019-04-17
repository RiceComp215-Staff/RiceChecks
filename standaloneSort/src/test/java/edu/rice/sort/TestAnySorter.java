/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.sort;

import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.*;

/** General infrastructure for testing any {@link edu.rice.sort.Sorter}. */
public class TestAnySorter {
  /** Runs QuickTheories on the sorter with random arrays of strings. */
  public static void exerciseStrings(Sorter<String> sorter) {
    qt().forAll(
            arrays()
                .ofStrings(strings().basicLatinAlphabet().ofLengthBetween(0, 10))
                .withLengthBetween(0, 100))
        .check(
            a -> {
              sorter.sortInPlace(a);
              return Sorter.isSorted(a);
            });
  }

  /** Runs QuickTheories on the sorter with random arrays of integers. */
  public static void exerciseIntegers(Sorter<Integer> sorter) {
    qt().forAll(arrays().ofIntegers(integers().all()).withLengthBetween(1, 100))
        .check(
            a -> {
              sorter.sortInPlace(a);
              return Sorter.isSorted(a);
            });
  }
}
