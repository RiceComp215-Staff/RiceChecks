[![Maven Central](https://maven-badges.herokuapp.com/maven-central/edu.rice.ricechecks/ricechecks/badge.png)](https://maven-badges.herokuapp.com/maven-central/edu.rice.ricechecks/ricechecks)
[![Build Status](https://github.com/RiceComp215-Staff/RiceChecks/workflows/RiceChecks%20Autograder/badge.svg)](https://github.com/RiceComp215-Staff/RiceChecks/actions)

# RiceChecks

**Current status**: early alpha. Please email <dwallach@rice.edu> before trying to use this.

---

This project contains an autograder for Java and Gradle-based student projects.
We try to simplify the process of specifying how unit tests and such
are mapped to points, leveraging Java annotations, so your grading policy
is embedded in your code. It's like JavaDoc, but for grading policies.

The output looks something like this for a successful project:
```
┌─────────────────────────────────────────────────────────────────────────────
│ Autograder for Sorting                                                      
│ Implement Many Sorting Algorithms                                           
├─────────────────────────────────────────────────────────────────────────────
│
│ HeapSort: 2 of 2 tests passed                                    2.0/2.0 ✅
│
│ InsertionSort: 2 of 2 tests passed                               2.0/2.0 ✅
│
│ PatienceSort: 2 of 2 tests passed                                2.0/2.0 ✅
│
│ ShellSort: 2 of 2 tests passed                                   2.0/2.0 ✅
│
│ No warning / style deductions                                    1.0/1.0 ✅
│
│ Test coverage meets 90% (by line) requirement                    1.0/1.0 ✅
│ - Coverage of edu.rice.sort.HeapSort: 100.0% (31/31)             
│ - Coverage of edu.rice.sort.InsertionSort: 100.0% (9/9)          
│ - Coverage of edu.rice.sort.PatienceSort: 100.0% (20/20)         
│ - Coverage of edu.rice.sort.PatienceSort.Pile: 100.0% (1/1)      
│ - Coverage of edu.rice.sort.ShellSort: 100.0% (14/14)            
│
├─────────────────────────────────────────────────────────────────────────────
│ Total points:                                                  10.0/10.0 ✅
└─────────────────────────────────────────────────────────────────────────────
```

Whereas, for a project with some bugs, you might see:
```
┌─────────────────────────────────────────────────────────────────────────────
│ Autograder for RPN                                                          
│ Simple RPN Calculator                                                       
├─────────────────────────────────────────────────────────────────────────────
│
│ Correctness: 0 of 2 tests passed                                 0.0/6.0 ❌
│ - edu.rice.rpn.RpnCalcTest.testBasicArithmetic: failed             (-3.0)
│ - edu.rice.rpn.RpnCalcTest.testStackHandling: failed               (-3.0)
│
│ Warning / style deductions                                       0.0/1.0 ❌
│ - CheckStyle (main): 0 of 1 files passed                         
│ - GoogleJavaFormat: 1 of 2 files passed                          
│   run the gradle <googleJavaFormat> task to fix
│
│ Classes with coverage below 90% (by line) requirement            0.0/3.0 ❌
│ - Coverage of edu.rice.rpn.RpnCalc: 73.8% (31/42)                
│   (including one anonymous inner class)
│ - See the coverage report for details:                           
│   ./build/reports/jacoco/index.html
│
├─────────────────────────────────────────────────────────────────────────────
│ Total points:                                                   0.0/10.0 ❌
└─────────────────────────────────────────────────────────────────────────────
```

## Table of contents
* [Concepts](#concepts)
* [Annotations](#annotations)
* [Coverage testing](#coverage-testing)
* [Sample projects](#sample-projects)
* [Student project integration](#student-project-integration)
* [Try it!](#try-it)
* [FAQs](#faqs)

## Concepts

The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values.
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your master branch. They have no impact on student code.
- You extract a *grading policy* for every given project, which you then include in the `config` directory
  that is handed out to your students.
  - This policy is a human-readable YAML file, so you can easily review it, for example, to
    ensure that you have the expected total number of points.
- You provide your students with a `build.gradle` file, specifying all the tasks that need to
  be run. This includes JUnit5 tests (`@Test` and `@TestFactory`), CheckStyle, google-java-format, ErrorProne, and JaCoCo.
- You add our provided autograder code to `build.gradle`, which runs all these gradle tasks,
  writing log files to the `build` directory.
  We change the default configuration so as many tasks will complete as possible, rather than
  stopping immediately when a task fails.
- The autograder runs
  as a standalone Java program, reading all the `build` output logs and evaluating them with respect
  to the grading policy. Results are printed to standard output, and then the autograder either
  exits with 0 or non-zero, which is then understood by
  CI services to imply success or failure.
- You configure your CI service to run all of this every time there's a push to your
  Git server. (For example, you might provide a `.travis.yml` file to
  arrange for Travis-CI to run the autograder on GitHub commits.)
- Exactly the same autograder run on the CI server as run locally on the students' computers.
  This means that students can rapidly see the autograder results, locally, and further
  benefit from the CI service's version to ensure they didn't forget to commit or push a file.
- Human graders can look at the CI output as well, transcribing this into the university's
  learning management system (e.g., Canvas), while also looking over the student projects
  for anything sketchy.
- We provide you several example projects so you can see how this all fits together.
    
RiceChecks, itself, is written in Kotlin, and should be able to process student projects written 
in Java, Kotlin or any other JVM language, although our focus is on Java-based student projects, 
at least for now. 

**RiceChecks, itself, is compiled with OpenJDK 8 and tested with both OpenJDK 8 and OpenJDK 11.
It's unlikely to work on earlier JDK releases.**

## Annotations

RiceChecks supports the following annotations:
- You specify one `@GradeProject` annotation per project. This
  annotation can appear on any Java class, or it can appear on a package (i.e., 
  inside a `package-info.java` file). For example, for a project on
  implementing sorting algorithms you might write:
```java
/* src/main/java/edu/rice/sort/package-info.java */
@GradeProject(
    name = "Sorting",
    description = "Implement Many Sorting Algorithms",
    warningPoints = 1.0,
    coveragePoints = 1.0,
    coveragePercentage = 90)
package edu.rice.sort;
```
  - The `warningPoints` are granted if all of the following
    checks come up clean:
    - [CheckStyle](http://checkstyle.sourceforge.net/) (enabled by default, disable with `useCheckStyle = false`)
    - [google-java-format](https://github.com/google/google-java-format) (enabled by default, disable with `useGoogleJavaFormat = false`)
    - [ErrorProne](http://errorprone.info/) (writes its output alongside the Java compiler warnings, see below)
    - [Javac's linter & compiler warnings](https://www.javaworld.com/article/2073587/javac-s--xlint-options.html) (enabled by default, disable with `useJavacWarnings = false`)
  - The `coveragePoints` and `coveragePercentage` are part of the *coverage policy*, detailed below.
  
- You then need one or more `GradeTopic` annotations:
```java
@GradeTopic(project = "Sorting", topic = "HeapSort")
public class HeapSortTest { /* ... */ }
```
```java
@GradeTopic(project = "Sorting", topic = "InsertionSort")
public class InsertionSortTest { /* ... */ }
```

  - `@GradeTopic` annotations can appear on any Java class or package.
    They allow you to create groupings of individual unit tests.
    You can specify an optional `maxPoints = N` attribute. If
    the individual unit tests associated with the topic add up
    to more than the given `maxPoints`, then those deductions
    will be capped at the given maximum number. If unspecified,
    the maximum number of points is computed based on all the
    unit tests associated with the topic.
    
- You then annotate each unit test with a `Grade` annotation:
```java
public class InsersionSortTest {
    @Test
    @Grade(project = "Sorting", topic = "InsertionSort", points = 1.0)
    public void insertionSortStrings() { /* ... */ }
}
```

- For JUnit5 test factories, which return a list of tests, whose
  length isn't known until runtime, you specify the number
  of points per test, and the maximum
  number of points for the whole list of tests:
  
```java
public class InsersionSortTest {
    @TestFactory
    @Grade(project = "RE", topic = "Numbers", points = 1, maxPoints = 5)
    List<DynamicTest> testIntegers() { /* ... */ }
}
```
## Annotation debugging and extraction
The process of writing down all these annotations can be tedious, and it's
easy to make mistakes. Once you think you're ready, run the `autograderDebugAnnotations` gradle
task, and something like this will appear on the console:

```yaml
name: "RPN"
description: "Simple RPN Calculator"
maxPoints: 10.0
warningPoints: 1.0
useCheckStyle: true
useGoogleJavaFormat: true
useJavacWarnings: true
coveragePoints: 3.0
coverageStyle: "LINES"
coveragePercentage: 90.0
coverageAnnotations:
- scope: "CLASS"
  excluded: false
  name: "edu.rice.rpn.RpnCalc"
topics:
- name: "Correctness"
  maxPoints: 6.0
  tests:
  - points: 3.0
    maxPoints: 0.0
    className: "edu.rice.rpn.RpnCalcTest"
    methodName: "testBasicArithmetic"
    testFactory: false
  - points: 3.0
    maxPoints: 0.0
    className: "edu.rice.rpn.RpnCalcTest"
    methodName: "testStackHandling"
    testFactory: false
```
Read it over and verify that what you see is consistent with what you expected. For example,
make sure that the `maxPoints` attribute at the top is what you intended. Once everything
is good, run the `autograderWriteConfig` task, which will place this same YAML contents into
`config/grade.yml`, which you would then distribute as part of the project to your students.

You might also delete the `autograderDebugAnnotations` and `autograderWriteConfig` tasks
from the `build.gradle` file before distributing it to the students to ensure they don't run
those tasks by accident. If you want, you could even delete all the RiceChecks annotations
(`@Grade`, etc.), although there's no harm in leaving them in.

## Coverage testing

RiceChecks uses
[JaCoCo](https://www.eclemma.org/jacoco/), which has a wide variety of ways that you can
configure its Gradle plugin to either
fail or pass the build based on different coverage policies. When we tried to just use
this as-is, students were unhappy with the feedback. The way we specify and report coverage
in RiceChecks is:

- The top-level coverage policy appears in the `GradeProject` annotation.
  - With `coveragePoints = N` you specify the number of grade points (awarded all or nothing)
    for satisfying the coverage policy.
  - You specify a `coveragePercentage` you wish to require of every covered class.
  - You may optionally specify a `coverageStyle`; your choices are `LINES` or `INSTRUCTIONS`,
    corresponding to the same terms as 
    [JaCoCo understands them](https://www.eclemma.org/jacoco/trunk/doc/counters.html). 
    If you're concerned that students might try to mash too much code onto a
    single line in order to game line-counting coverage, you might
    prefer the `INSTRUCTIONS` mode, which is based on Java bytecode operations,
    or you could require *google-java-format*, described above, which forces
    code to be formatted in a more reasonable fashion.

- You annotate Java classes or packages (via `package-info.java`) with an `@GradeCoverage`
  annotation to note which project(s) care about coverage for those Java classes or packages.
```java
@GradeCoverage(project = "Sorting")
public class HeapSort { /* ... */ }
```
- You can set an `exclude = true` flag on the `GradeCoverage` annotation if you want to say that a particular class is
  *not* to be considered for coverage testing. This might make sense if
  you've enabled coverage testing on an entire package but wish to
  exclude a specific Java class within the package for coverage testing. A `GradeCoverage` annotation
  applies recursively to inner classes as well.
  If there are multiple applicable coverage annotations external to a class, the closest one
  wins.
- Each class (or inner class) is evaluated for its coverage, for the desired metric,
  independently. *Every* class must pass for RiceChecks to award the coverage points.
- What about lambdas or methods? JaCoCo measures per-method coverage, treating every lambda as
  if it's a separate method within the same class. RiceChecks only looks
  at the per-class summary data, which wraps up all methods and lambdas within that class.
  
- For any inner class, once we've determined that it's subject to coverage testing, it will
  be measured for hitting the desired coverage level on its own, without any dependencies
  on its outer or further-inner classes.

- For *anonymous* inner classes, contained inside a class that's subject to coverage,
  we *accumulate* the statistics from the anonymous inner class with its containing class,
  enforcing coverage requirements on their statistics' sums. (Since anonymous inner classes
  don't have names, this helps us avoid giving unhelpful feedback to a student.)
  
## Sample projects
There are three sample projects, showing you how the RiceChecks autograder works. They
are:
- [exampleRegex](exampleRegex): students are asked to implement several regular expressions;
  their work is tested using JUnit5's [TestFactory](https://junit.org/junit5/docs/5.4.0/api/org/junit/jupiter/api/TestFactory.html),
  which has a list of examples for each regex that should be accepted and another list
  of examples that should be rejected by the regex.
- [exampleRpn](exampleRpn): students are asked to implement a simple RPN calculator;
  there are many cases, so we require minimum test coverage.
- [exampleSort](exampleSort): students are asked to implement four sorting algorithms;
  their work is tested with [QuickTheories](https://github.com/quicktheories/QuickTheories),
  generating hundreds of random inputs.
  
All of the code for these examples is borrowed from [RosettaCode](http://rosettacode.org/wiki/Rosetta_Code),
to which we added our own unit tests and made other small changes. The code,
as you view it in this repository, passes all tests and gets a perfect grade.
You might try modifying one or more of the examples, introducing bugs, to
see how the autograder responds.

## Student project integration

The "example" projects have their `build.gradle` files configured to
compile and use RiceChecks from the adjacent sources. When configuring a student
repository for use with RiceChecks, you should start with
[standaloneSort/build.gradle](standaloneSort/build.gradle),
which has the following features:

- Loads `edu.rice.ricechecks:ricechecks-annotations:0.7.3` (just the Java annotations)
  as a regular dependency for student code.
  
- Loads `edu.rice.ricechecks:ricechecks:0.7.3` (the autograder tool) as part of
  a separate Gradle "configuration", ensuring that symbols from the tool don't accidentally
  autocomplete in students' IDEs.
  
Provides three Gradle tasks that invoke the autograder tool:
  - `autograderDebugAnnotations` -- this allows you to see the result of processing your annotations. You might verify, for example, that
     you had the desired number of total points. You might use this for yourself
     but delete the task from `build.gradle` before shipping your repo to
     the students.
  - `autograderWriteConfig` -- when you're happy with your annotations,
     this writes a YAML file to the config directory which is used by
     the main grading task later on. As with `autograderDebugAnnotations`,
     you might choose not to ship this to your students.
  - `autograder` -- this runs everything -- compiling the code, running
     the unit tests, and collecting all the coverage results --
     and prints a summary to the console.
  
Ultimately, the `autograder` task replaces what might normally be a call to
`gradlew check` in places like a Travis-CI `.travis.yml` file.
  
## Try it!
We took our three sample projects and created standalone repositories, which
you might then clone and experiment with.

- https://github.com/RiceComp215-Staff/RiceChecks-SortExample
- https://github.com/RiceComp215-Staff/RiceChecks-RegexExample
- https://github.com/RiceComp215-Staff/RiceChecks-RpnExample

## FAQs

- **Are there other Gradle-based Java autograders?**
  - [Illinois CS125 GradleGrader](https://github.com/cs125-illinois/gradlegrader): the direct inspiration for RiceChecks
  - [GatorGrader](https://github.com/GatorEducator/gatorgrader)
  - [JGrade](https://github.com/tkutche1/jgrade)
  
  And see also:
  - [Autogradr](https://www.autogradr.com/)
  - [code-check](https://bitbucket.org/danielmai/code-check-homework-grading)
  - [CodeHS](https://codehs.com/)
  - [GradeScope Autograder](https://gradescope-autograders.readthedocs.io/en/latest/java/)
  - [Vanderbilt Grader](https://mvnrepository.com/artifact/edu.vanderbilt.grader/gradle-plugin/1.4.64) / [Rubric](https://mvnrepository.com/artifact/edu.vanderbilt.grader/rubric/1.1) (used in their [Android MOOC](https://www.coursera.org/specializations/android-app-development))
  
- **Why Gradle?** The short answer: because it's popular and widely supported.
  Among other things, Gradle is widely used for Android applications. 
  Whenever somebody comes out with a clever Java-related tool, they generally
  release a Gradle plugin for it. Students don't have to learn or understand
  Gradle to use RiceChecks. IntelliJ provides a "Gradle" tab on which students
  can click on the tasks they wish to run, including the `autograder` task
  provided by RiceChecks.
  
- **Can you build a Gradle plugin so I don't need all this custom code in the `build.gradle` file?**
  You're welcome to have a go at it and submit a PR. I'm concerned about how to write such
  a thing in a general-purpose way, given all the different ways that different
  projects will configure Gradle. RiceChecks, by running as a completely separate
  Java process, avoids getting too entangled with Gradle, beyond knowing how all
  its log files are written.
  
- **Does RiceChecks work with {JUnit4, TestNG, ...}?** Maybe? What really
  matters is how Gradle's test unit runner writes an XML log of its results
  into the `build` directory. If you use some other mechanism for
  running tests, then you'll need to extend the logic in
  [JUnitScanner.kt](https://github.com/RiceComp215-Staff/RiceChecks/blob/master/autograder/src/main/kotlin/edu/rice/autograder/JUnitScanner.kt).
  
- **RiceChecks only really supports JUnit5's `@Test` and `@TestFactory`. What will
  it take to more broadly support other JUnit5 test annotations?**
  Most custom JUnit5 annotations that say "here's a test" could
  potentially be something we can treat as
  equivalent to either `@Test` or `@TestFactory`. RiceChecks
  hasn't yet been tested with some of the fancier JUnit5 features
  like multiple `dynamicTest` instances inside of a `dynamicContainer`.
  To support
  [meta annotations](https://junit.org/junit5/docs/current/user-guide/#writing-tests-meta-annotations)
  or custom annotations from a project like [Karate](https://github.com/intuit/karate#junit-5),
  RiceChecks would probably need its own meta annotation facility to tell it what
  to do. If there's a custom test runner involved, things will get more complicated.
  
- **What if I want to use something other than the `assert` statements
  built into JUnit5?** RiceChecks only cares about whether
  a test method succeeds or fails. You can use anything
  inside of those methods. In our own tests, we're sometimes
  using [QuickTheories](https://github.com/quicktheories/QuickTheories),
  which does its own internal assertions. Gradle knows how to run the
  tests and it all looks the same when RiceChecks reads the logs.
  
- **What about [Spotless](https://github.com/diffplug/spotless) or [SpotBugs](https://github.com/spotbugs/spotbugs)?**
  Spotless is analogous to [CheckStyle](http://checkstyle.sourceforge.net/) and [google-java-format](https://github.com/google/google-java-format). 
  SpotBugs is analogous to [ErrorProne](https://errorprone.info/). You could certainly engineer support for additional tooling
  into RiceChecks, but it's not here right now.
  
- **Why are you using both CheckStyle and google-java-format?** The nice thing
  about google-java-format is that it provides an auto-indenter that students
  can run as a Gradle task (`googleJavaFormat`). We still need CheckStyle to enforce other useful Java practices,
  like capital names for classes with matching filenames. CheckStyle saves us from weird
  scenarios where a Java program compiles on a case-insensitive filesystem (Windows or Mac) but not on
  a case-sensitive filesystem (Linux). 

- **Why do you write out the grading policy to a YAML file? Why not just
  re-read the annotations every time?** Let's say you want to have "secret" unit
  tests that you don't initially give to your students, perhaps because you want
  to make them write their own tests before seeing yours. You can construct a policy
  with your tests present, save it to the YAML file, and then delete your "secret" Java test file prior
  to distributing the project to your students. When the student runs the autograder,
  it will notice that the "secret" tests are missing and treat them as having failed.
  When you later add them back in, everything works.
  
  Of course, we still rely on human graders to notice if a student edited
  the YAML file, or for that matter, edited the unit tests we provided to them.
  
- **How do you add "secret" test files into student projects after an assignment is live?** 
  We hand out our
  weekly projects on Monday morning with student unit tests due Thursday evening.
  On Friday morning, we pull every student repository, add the "secret" tests,
  commit, and push. Students will then have the benefit of both their tests as
  well as ours to make sure they get their submission solid before the Sunday evening deadline.
  If you prefer your students not to see your "secret" tests prior to the deadline, you
  could always do a similar process *after* the final submission deadline.
  
- **Why did you write the autograder itself in Kotlin?** An important
  motivating factor was being able to easily leverage the efforts
  of the [ClassGraph](https://github.com/classgraph/classgraph) project, which makes it
  straightforward to extract annotations from Java code, and [Jackson](https://github.com/FasterXML/jackson),
  which has simple support for reading and writing XML, YAML, JSON, and a variety of other common formats.
  Since both ClassGraph and Jackson are just Java libraries,
  we could call them from Java, Kotlin, Scala, or any other JVM language. 
  
  Since students will never need to see or understand the code for the autograder,
  we can use any JVM language, so we chose Kotlin. Kotlin gives an entirely pleasant
  experience for building a tool like RiceChecks. We also take advantage of the
  [Arrow](https://arrow-kt.io/) functional programming library.
  
- **Java8 versus Java11 versus...** We want to support student projects written in Java8 or Java11,
  and eventually newer versions of Java as well. To that end, we compile RiceChecks using
  OpenJDK 8 and test it with both OpenJDK 8 (the examples within this repository) and OpenJDK 11
  (the standalone demonstrations linked from the [Try it!](#try-it) section). 
  
  To support Java11, you'll notice several minor differences
  in those examples' `build.gradle` files, but RiceChecks runs the same.
  
  RiceChecks is dependent on how the various Gradle plugins write out their log files.
  That means that upgrading to a newer version of Gradle or of any of the plugins has the
  potential to break RiceChecks.
  
- **Which Java distribution are you using?** [Amazon Corretto](https://aws.amazon.com/corretto/)
  supports OpenJDK 8 and 11, providing up-to-the-minute bug fixes, and is used by Amazon 
  for their own production services. Amazon also provides
  `pkg` files for Apple and `msi` files for Windows, allowing students to double-click
  and install. For Travis-CI, we just specify `openjdk8` or `openjdk11` and whatever
  they use seems to work just fine.

- **Can I have machine-readable output from RiceChecks / Can RiceChecks 
  send grades automatically to my server?** 
  After the autograder runs, it creates two files: 
  `build/autograder/report.json` and `build/autograder/report.yml`
  (same data, your choice of serialization format). The information is a superset of 
  the pretty-printed output, which tries to only print what the user really needs (e.g., only
  printing the names of failed unit tests, rather than the names of every unit test). 
  
  After the autograder has finished running, your own code can pick up the autograder report
  and take further actions, like uploading it to a server. Keep in mind that students control
  their repositories, which includes the possibility of generating deliberately incorrect reports.
  
- **How can I do coverage testing on a per-method basis rather than per-class?** 
  You could extend the relevant code in
  [JacocoScanner.kt](https://github.com/RiceComp215-Staff/RiceChecks/blob/master/autograder/src/main/kotlin/edu/rice/autograder/JacocoScanner.kt),
  which is already getting a bit complicated, to enforce more
  complicated coverage policies. You'd also have to reconfigure
  the annotation system to allow `@GradeCoverage` annotations on
  methods, and you'd need to figure out what to do about lambdas
  and anonymous inner classes.
  
- **On Windows, when I run the autograder, I see a bunch of `?????`'s rather than the nice Unicode
  borders around the autograder output. How do I fix that?** 
  
  - For IntelliJ, you can go to *Help* ➔ *Edit Custom VM Options...* and add the line
   `-Dfile.encoding=UTF-8`. Restart IntelliJ and the Unicode should all work properly. 
   
  - For Windows console users, check out Microsoft's new 
    [Windows Terminal](https://devblogs.microsoft.com/commandline/introducing-windows-terminal/).
    It's free and it seems to have lots of promising features.
    
  - For Mac users, the regular Terminal program, installed on every Mac, seems
    to do the right thing out of the box.
