package edu.rice.autogradertest;

import edu.rice.autograder.GradeCoverage;

@GradeCoverage(project = "TP3")
public class Project3 {
  static long factorial(int n) {
    if (n <= 1) {
      return 1;
    } else {
      return n * factorial(n - 1);
    }
  }

  static long choose(int n, int r) {
    return factorial(n) / (factorial(n - r) * factorial(r));
  }
}
