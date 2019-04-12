/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.rpn;

import edu.rice.autograder.annotations.GradeCoverage;
import java.util.ArrayDeque;

@GradeCoverage(project = "RPN")
class RpnCalc {
  // Try replacing this with LinkedList, and the code will all run correctly,
  // but ErrorProne will generate a warning.
  ArrayDeque<Double> stack = new ArrayDeque<>();

  // Code here borrowed from RosettaCode and then changed to make it more
  // amenable for unit testing.
  // http://www.rosettacode.org/wiki/Parsing/RPN_calculator_algorithm#Java_2

  public RpnCalc() {}

  public String eval(String expr) {
    // Try eliminating the optional "-1" argument to split and
    // you'll see ErrorProne generate a warning, but all the
    // unit tests will still pass.
    for (String token : expr.split("\\s+", -1)) {
      Double tokenNum;
      try {
        tokenNum = Double.parseDouble(token);
      } catch (NumberFormatException e) {
        tokenNum = null;
      }
      if (tokenNum != null) {
        stack.push(Double.parseDouble(token + ""));
      } else {
        double firstOperand;
        double secondOperand;

        switch (token) {
          case "*":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(firstOperand * secondOperand);
            break;
          case "/":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(firstOperand / secondOperand);
            break;
          case "-":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(firstOperand - secondOperand);
            break;
          case "+":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(firstOperand + secondOperand);
            break;
          case "^":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(Math.pow(firstOperand, secondOperand));
            break;
          case "dup":
            stack.push(stack.getFirst());
            break;
          case "drop":
            stack.pop();
            break;
          case "swap":
            secondOperand = stack.pop();
            firstOperand = stack.pop();
            stack.push(secondOperand);
            stack.push(firstOperand);
            break;
          default:
            // just in case
            throw new RuntimeException("Unknown operation");
        }
      }
    }
    return stack.isEmpty() ? "Empty" : stack.getFirst().toString();
  }
}
