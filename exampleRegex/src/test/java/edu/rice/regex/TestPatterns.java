/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.regex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import edu.rice.autograder.annotations.Grade;
import edu.rice.autograder.annotations.GradeProject;
import edu.rice.autograder.annotations.GradeTopic;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

@GradeProject(name = "RE", description = "Writing regular expressions", warningPoints = 1.0)
@GradeTopic(project = "RE", topic = "Identifiers")
@GradeTopic(project = "RE", topic = "Numbers")
public class TestPatterns {
  @Grade(project = "RE", topic = "Identifiers", points = 0.5, maxPoints = 1.5)
  @TestFactory
  List<DynamicTest> testClassIdentifiers() {
    List<String> goodClassNames =
        Arrays.asList("Alice", "Bob", "Charlie", "CamelCaseClasses", "Thing1", "Thing2");
    List<String> badClassNames =
        Arrays.asList(
            "alice",
            "bob",
            "charlie",
            "camelCaseClasses",
            "xThing1",
            "xThing2",
            "Thing$Thing",
            "Thing Thing",
            " Thing",
            "Thing ",
            "Thing_Thing");

    return goodAndBad(goodClassNames, badClassNames, Patterns.classPattern);
  }

  @Grade(project = "RE", topic = "Identifiers", points = 0.5, maxPoints = 1.5)
  @TestFactory
  List<DynamicTest> testMethodIdentifiers() {
    List<String> goodMethodNames =
        Arrays.asList("alice", "bob", "charlie", "camelCaseNames", "thing1", "thing2");
    List<String> badMethodNames =
        Arrays.asList(
            "Alice",
            "Bob",
            "Charlie",
            "CamelCaseNames",
            "XThing1",
            "XThing2",
            "thing$Thing",
            "thing Thing",
            " thing",
            "thing ",
            "thing_Thing");

    return goodAndBad(goodMethodNames, badMethodNames, Patterns.methodPattern);
  }

  @Grade(project = "RE", topic = "Numbers", points = 1, maxPoints = 1)
  @TestFactory
  List<DynamicTest> testIntegerUnderscores() {
    List<String> goodNumbers = Arrays.asList("234_567", "2_3_4_5_6_7", "-234_567_890L");
    List<String> badNumbers = Arrays.asList("_234_567", "2__3_4_5_6_7", "8_");

    return goodAndBad(goodNumbers, badNumbers, Patterns.integerPattern);
  }

  @Grade(project = "RE", topic = "Numbers", points = 1, maxPoints = 5)
  @TestFactory
  List<DynamicTest> testIntegers() {
    List<String> goodNumbers = Arrays.asList("-29", "-3", "-0", "0", "1", "22", "9285", "0L", "1L");
    List<String> badNumbers =
        Arrays.asList(
            "-029", "-03", "-00", "00", "0-", "0-0", "1f", "0x22", "9285.3", "10e5", "NaN");

    return goodAndBad(goodNumbers, badNumbers, Patterns.integerPattern);
  }

  private static List<DynamicTest> goodAndBad(
      List<String> goodExamples, List<String> badExamples, String regex) {

    Predicate<String> p = Pattern.compile("^" + regex + "$").asPredicate();
    // Pattern.compile(regex).asMatchPredicate(); // only works in newer JDKs

    Stream<DynamicTest> positiveTests =
        goodExamples.stream().map(s -> dynamicTest("Good: " + s, () -> assertTrue(p.test(s))));
    Stream<DynamicTest> negativeTests =
        badExamples.stream().map(s -> dynamicTest("Bad: " + s, () -> assertFalse(p.test(s))));

    return Stream.concat(positiveTests, negativeTests).collect(Collectors.toList());
  }
}
