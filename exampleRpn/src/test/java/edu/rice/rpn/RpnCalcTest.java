/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.rpn;

import static org.junit.jupiter.api.Assertions.*;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@GradeProject(
    name = "RPN",
    description = "Simple RPN Calculator",
    warningPoints = 1.0,
    coveragePoints = 3.0,
    coveragePercentage = 90)
@GradeTopic(project = "RPN", topic = "Correctness")
class RpnCalcTest {
  @Test
  @Grade(project = "RPN", topic = "Correctness", points = 3.0)
  void testBasicArithmetic() {
    RpnCalc rpnCalc = new RpnCalc();
    assertEquals("7.0", rpnCalc.eval("3 4 +"));
    assertEquals("12.0", rpnCalc.eval("3 4 *"));
    assertEquals("-1.0", rpnCalc.eval("3 4 -"));
    assertEquals("0.75", rpnCalc.eval("3 4 /"));
    assertEquals("8.0", rpnCalc.eval("2 3 ^"));
  }

  @Test
  @Grade(project = "RPN", topic = "Correctness", points = 3.0)
  void testStackHandling() {
    RpnCalc rpnCalc = new RpnCalc();
    assertEquals("7.0", rpnCalc.eval("3 4 +"));
    assertEquals("Empty", rpnCalc.eval("drop"));
    assertThrows(NoSuchElementException.class, () -> rpnCalc.eval("drop"));
    assertEquals("4.0", rpnCalc.eval("1 2 3 4"));
    assertEquals("3.0", rpnCalc.eval("drop"));
    assertEquals("2.0", rpnCalc.eval("drop"));
    assertEquals("1.0", rpnCalc.eval("drop"));
    assertEquals("Empty", rpnCalc.eval("drop"));
    assertEquals("1.0", rpnCalc.eval("3 4 swap -"));
    assertEquals("3.0", rpnCalc.eval("2 6 swap /"));
  }
}
