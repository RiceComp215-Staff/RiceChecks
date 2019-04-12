# RiceChecks
This project contains an autograder for Java and Gradle-based student projects.
We try to simplify the process of specifying how unit tests and such
are mapped to points, leveraging Java annotations, so your grading policy
is embedded in your code. It's like JavaDoc, but for grading policies.

The output looks something like this for a successful project:
```
┌─────────────────────────────────────────────────────────────────────────────
│ RiceChecks for Sorting                                                      
│ Implement Many Sorting Algorithms                                           
├─────────────────────────────────────────────────────────────────────────────
│
│ Topic: HeapSort: 2 of 2 tests passed                             2.0/2.0 ✅
│
│ Topic: InsertionSort: 2 of 2 tests passed                        2.0/2.0 ✅
│
│ Topic: PatienceSort: 2 of 2 tests passed                         2.0/2.0 ✅
│
│ Topic: ShellSort: 2 of 2 tests passed                            2.0/2.0 ✅
│
│ No warning / style deductions                                    1.0/1.0 ✅
│
│ Test coverage meets 90% (by line) requirement                    1.0/1.0 ✅
│ - Coverage of edu.rice.sort.HeapSort: 100.0%                     
│ - Coverage of edu.rice.sort.InsertionSort: 100.0%                
│ - Coverage of edu.rice.sort.PatienceSort: 100.0%                 
│ - Coverage of edu.rice.sort.PatienceSort.Pile: 100.0%            
│
├─────────────────────────────────────────────────────────────────────────────
│ Total points:                                                  10.0/10.0 ✅
└─────────────────────────────────────────────────────────────────────────────
```

Whereas, for a project with some bugs, you might see:
```
┌─────────────────────────────────────────────────────────────────────────────
│ RiceChecks for RPN                                                          
│ Simple RPN Calculator                                                       
├─────────────────────────────────────────────────────────────────────────────
│
│ Topic: Correctness: 1 of 2 tests passed                          3.0/6.0 ❌
│ - edu.rice.rpn.RpnCalcTest.testBasicArithmetic: passed           
│ - edu.rice.rpn.RpnCalcTest.testStackHandling: failed               (-3.0)
│
│ Warning / style deductions                                       0.0/1.0 ❌
│ - CheckStyle (main): 0 of 1 files passed                           (-1.0)
│ - CheckStyle (test): 1 of 1 files passed                         
│ - GoogleJavaFormat: 1 of 2 files passed                            (-1.0)
│   run the gradle <googleJavaFormat> task to fix
│ - Compiler: No warnings or errors                                
│
│ Classes with coverage below 90% (by line) requirement            0.0/3.0 ❌
│ - Coverage of edu.rice.rpn.RpnCalc: 78.9%                        
│ - See the coverage report for details:                           
│   ./build/reports/jacoco/index.html
│
├─────────────────────────────────────────────────────────────────────────────
│ Total points:                                                   3.0/10.0 ❌
└─────────────────────────────────────────────────────────────────────────────
```

You wire the RiceChecks autograder into every student's `build.gradle` file, and they'll
be able to generate these reports locally, any time they want. You can also easily configure
the autograder to run with a CI service, like Travis-CI, on every commit.

## HOWTO

The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your master branch.
- You extract a *grading policy* for every given project, which you then include in the `config` directory
  that is handed out to your students
  - This policy is a human-readable YAML file, so you can easily review it, for example, to
    ensure that you have the expected total number of points.
- You provide your students with a `build.gradle` file, specifying all the actions that need to
  be run. This includes JUnit5 tests (`@Test` and `@TestFactory`), CheckStyle, google-java-style, ErrorProne, and JaCoCo.
- You add the autograder code to `build.gradle` that we provide, which runs all these gradle actions,
  each of which already logs their output to the `build` directory, making sure that a 
  failure of one task doesn't preclude the rest of the tasks from running. 
- The autograder runs
  as a standalone Java program, reading all this output and evaluating it with respect
  to the grading policy, printing its results to standard output, and then it either
  exits with 0 or non-zero, which is then understood by
  CI services to imply success or failure.
- You configure your CI service to run all of this every time there's a push to your
  Git server. (For example, you might provide a `.travis.yml` file to
  arrange for Travis-CI to run the autograder on GitHub commits.)
- We provide you several example projects so you can see how this all fits together.
    
RiceChecks, itself, is written in Kotlin, and should be able to process student projects written in Java, Kotlin
or any other JVM language,
although our focus is on Java-based student projects, at least for now. 
Notably, RiceChecks writes its grading conclusions to the console as human-readable
text. Our human graders read this text from the CI system's logs, and then transcribe these
numbers into our university learning management system (Canvas), while also looking over the
student projects to ensure that they didn't do something sketchy.

**RiceChecks, itself, is only tested against OpenJDK 11. It's unlikely to work on earlier JDK releases.**

## Directory structure
The `autograder` directory includes our autograder implementation (in Kotlin) and annotations (Java)
as well as a pile of unit tests. The other top-level directories (starting with `example`) 
are demonstration projects. 

In any of the example projects, you can run the `autograder` task, which will
in turn run time compiler, unit tests, and so forth, printing the ultimate
output to the console.

## Annotations

RiceChecks supports the following annotations:
- You specify one `@GradeProject` annotation per project. This
  annotation can appear on any class, or it can appear on a package (i.e., 
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
    - [GoogleJavaFormat](https://github.com/google/google-java-format) (enabled by default, disable with `useGoogleJavaFormat = false`)
    - [ErrorProne](http://errorprone.info/) (writes its output alongside the Java compiler warnings, see below)
    - [Javac's linter & compiler warnings](https://www.javaworld.com/article/2073587/javac-s--xlint-options.html) (enabled by default, disable with `useJavacWarnings = false`)
  - The `coveragePoints` and `coveragePercentage` are part of the *coverage policy*, detailed below.
  
- You then need one or more `GradeTopic` annotations:
```java
@GradeTopic(project = "Sorting", topic = "HeapSort")
public class HeapSortTest { ... }
```
```java
@GradeTopic(project = "Sorting", topic = "InsertionSort")
public class InsertionSortTest { ... }
```

  - `@GradeTopic` annotations can appear on any class or package.
    They allow you to create groupings of individual unit tests.
    You can specify an optional `maxPoints = N` attribute. If
    the individual unit tests associated with the topic add up
    to more than the given `maxPoints`, then those deductions
    will be capped at the given maximum number. If unspecified,
    the maximum number of points is computed based on all the
    unit tests associated with the topic.
    
- You then annotate each unit test with a `Grade` annotation:
```java
@Test
@Grade(project = "Sorting", topic = "InsertionSort", points = 1.0)
public void insertionSortStrings() { ... }
```

- For JUnit5 test factories, which return a list of tests, whose
  length isn't known until runtime, you specify the number
  of points per test, and the maximum
  number of points for the whole list of tests:
  
```java
  @Grade(project = "RE", topic = "Numbers", points = 1, maxPoints = 5)
  @TestFactory
  List<DynamicTest> testIntegers() { ... }
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
`config/grade.yaml`, which you would then distribute as part of the project to your students.

You might also delete the `autograderDebugAnnotations` and `autograderWriteConfig` tasks
from the `build.gradle` file before distributing it to the students to ensure they don't run
those tasks by accident. If you want, you could even delete all the RiceChecks annotations
(`@Grade`, etc.), although there's no harm in leaving them in.

## About coverage testing

RiceChecks uses
[JaCoCo](https://www.eclemma.org/jacoco/), which has a wide variety of ways that you can
configure its Gradle plugin to either
fail or pass the build based on different coverage policies. When we tried to just use
this as-is, students were wildly unhappy with the feedback. The way we specify and report coverage
in RiceChecks is:

- The top-level coverage policy appears in the `GradeProject` annotation.
  - With `coveragePoints = N`
you specify the number of grade points (awarded all or nothing) for satisfying the coverage policy.
  - You specify a `coveragePercentage` you wish to require of every covered class.
  - You may optionally specify a `coverageStyle`, and your choices are `LINES` or `INSTRUCTIONS`,
    corresponding to the same terms as JaCoCo understands them. If you're
    concerned that students might try to mash too much code onto a
    single line in order to thwart line-counting coverage, you might
    prefer the `INSTRUCTIONS` mode, which is based on Java bytecode operations,
    or you could require *GoogleJavaFormat*, described above, which would
    reject code written with such shenanigans.

- You annotate packages (via `package-info.java`) or classes with an `@GradeCoverage`
  annotation to note which project(s) care about coverage for those classes or packages.
```java
@GradeCoverage(project = "Sorting")
public class HeapSort { ... }
```
- You can set an `exclude = true` flag on the `GradeCoverage` annotation if you want to say that a particular class is
  *not* to be considered for coverage testing. This might make sense if
  you've enabled coverage testing on an entire package but wish to
  exclude a specific class within the package for coverage testing. A `GradeCoverage` annotation
  applies recursively to inner classes as well.
  If there are multiple applicable coverage annotations external to a class, the closest one
  wins.
- Each class (or inner class) is evaluated for its coverage, for the desired metric,
  independently. *Every* class must pass for RiceChecks to award the coverage points.
  
## Integrating the autograder into your Gradle projects

- There are three different Jar files (built by the `gradlew allJars` task):
  - `RiceChecks-0.1.jar` -- a "thin" Jar file, including the annotations and the autograder, but
    without its external dependencies. Suitable for executing from a Gradle environment, which will
    recursively fetch any dependencies.
  - `RiceChecks-fat-0.1.jar` -- a "fat" Jar file, including the annotations, the autograder, and
    *all of the recursive dependencies of the autograder*. If you want to be able to run the autograder
    directly from the command-line (e.g., `java -jar RiceChecks-fat-0.1.jar --project p1 grade`),
    then this Jar file has everything necessary.
  - `RiceChecks-annotations-0.1.jar` -- a tiny Jar file, including *only* the annotations
    and nothing else. This is the only dependency you want to be visible to student projects.
    (You don't want, for example, that students' IDEs will autocomplete into the guts of the
     autograder implementation.)

- Our example gradle projects directly include the autograder
  classes from the subproject where they're compiled. See the
  `exampleStandalone` project for an alternative, which pulls
  in the autograder like anything else from a Maven server.
  
- There are three ways to run the RiceChecks autograder, which you
  can see embodied in three Gradle tasks: 
  - `autograderDebugAnnotations` -- this allows you to see the result of processing your annotations. You might verify, for example, that
     you had the desired number of total points.
  - `autograderWriteConfig` -- when you're happy with your annotations,
     this writes a YAML file to the config directory which is used by
     the main grading task later on.
  - `autograder` -- this runs everything and prints the results to the console
  
- Ultimately, the `gradlew autograde` action replaces what might normally be a call to
  `gradlew check`. 
  
## FAQs

- **Are there other Gradle-based Java autograders?**
  - [Illinois CS125 GradleGrader](https://github.com/cs125-illinois/gradlegrader): the direct inspiration for RiceChecks
  - [GatorGrader](https://github.com/GatorEducator/gatorgrader)
  - [Vanderbilt Grader](https://mvnrepository.com/artifact/edu.vanderbilt.grader/gradle-plugin/1.4.3)
  - [GradeScope Autograder](https://gradescope-autograders.readthedocs.io/en/latest/java/)
  - [JGrade](https://github.com/tkutche1/jgrade)
  
- **Why Gradle?** The short answer: because it's popular and widely supported.
  Among other things, Gradle is now the standard build tool for Android applications. 
  Whenever somebody comes out with a clever Java-related tool, they generally
  release a Gradle plugin for it. Students don't have to learn or understand
  Gradle to use `RiceChecks`. IntelliJ provides a "Gradle" tab on which students
  can click on the tasks they wish to run.

- **Why do you write out the grading policy to a YAML file? Why not just
  re-read the annotations every time?** Let's say you want to have "secret" unit
  tests that you don't initially give to your students. You can construct a policy
  with them present, save it to the YAML file, and then delete the file prior
  to distributing the project to your students. When the student runs the autograder,
  it will notice that the "secret" tests are missing and treat them as having failed.
  When you later add them back in, everything works.
  
- **How do you add files into an active student project like this?** We hand out our
  weekly projects on Monday morning with student unit tests due Thursday evening.
  On Friday morning, we pull every student repository, add the "secret" tests,
  commit, and push. Students will then have the benefit of both their tests as
  well as ours to make sure they get their submission solid before the Sunday evening deadline.
  If you prefer your students not to see your "secret" tests prior to the deadline, you
  could always do a similar process *after* the final submission deadline.
  
- **Why did you write the autograder itself in Kotlin as opposed to ...?** One main
  motivating factor in the RiceChecks design was being able to easily leverage the efforts
  of the [ClassGraph](https://github.com/classgraph/classgraph) project, which makes it
  straightforward to extract annotations from Java code, and [Jackson](https://github.com/FasterXML/jackson),
  which has support for reading and writing XML, YAML, and a variety of other common formats.
  Since both ClassGraph and Jackson are just Java libraries,
  we could call them from Java, Kotlin, Scala, or any other JVM language. 
  We're not expecting students to work on this code, so we picked Kotlin, since
  it tends to be more concise than Java while still being easy to read for
  an experienced Java programmer.
  
- **Can I hack together machine-readable output from RiceChecks / Can I hack RiceChecks to
  send grades automatically to my server?** Have a look at [Aggregators.kt](blob/master/autograder/src/main/kotlin/edu/rice/autograder/Aggregators.kt),
  which collects together a `List<EvaluatorResult>` and prints it. You'd
  instead want to convert that list to your favorite format and then operate on it.
  We decided not to do this because we wanted to have human graders in the
  loop for things that we cannot automatically check (e.g., whether a design
  is "good") and to make sure that students weren't doing something undesirable,
  like editing our provided unit tests.

- **On Windows, when I run the autograder, I see a bunch of ?????'s rather than the nice borders around the autograder output. How do I fix that?** 
  For IntelliJ, you can go to *Help* -> *Edit Custom VM Options...* and add the line `-Dfile.encoding=UTF-8`. Restart IntelliJ and the Unicode should
  all work properly. For the Windows console, [it's complicated](https://devblogs.microsoft.com/commandline/windows-command-line-unicode-and-utf-8-output-text-buffer/).
  Maybe install a [different console program](https://conemu.github.io/en/UnicodeSupport.html)?