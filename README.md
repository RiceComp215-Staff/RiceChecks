# RiceChecks
This project contains an autograder for Java and Gradle-based student projects.
We try to simplify the process of specifying how unit tests and such
are mapped to points, leveraging Java annotations, so your grading policy
is embedded in your code. It's like JavaDoc, but for grading policies.

The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your master branch.
- You extract a *grading policy* for every given project, which you then include in the `config` directory
  that is handed out to your students
  - This policy is a human-readable YAML file, so you can easily review it, for example, to
    ensure that you have the expected total number of points.
- You provide your students with a `build.gradle` file, specifying all the actions that need to
  be run. This includes JUnit4/5 tests, CheckStyle, google-java-style, ErrorProne, and JaCoCo.
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
    
RiceChecks, itself, is written in Kotlin, and should be able to process student projects written in Java or Kotlin,
although our focus is on Java-based student projects, at least for now. 
Notably, RiceChecks writes its grading conclusions to standard-out as human-readable
text. Our human graders read this text from the CI system's logs, and then transcribe these
numbers into our university learning management system (Canvas), while also looking over the
student projects to ensure that they didn't do something sketchy.

**RiceChecks, itself, is only tested against OpenJDK 11. It's unlikely to work on earlier JDK releases.
Student code running with earlier Java releases should work just fine, although we do everything on the same version.**


## Directory structure
The `autograder` directory includes our autograder implementation (in Kotlin) and annotations (Java)
as well as a pile of unit tests. The other top-level directories (starting with `example`) 
are demonstration projects. 

In any of the example projects, you can run the `autograder` task, which will
in turn run time compiler, unit tests, and so forth, printing the ultimate
output to the console.

## Annotations

RiceChecks supports the following annotations:
- `@GradeProject`: you specify one of these per project. This
  annotation can appear on any class, or it can appear on a package (i.e., 
  inside a `package-info.java` file). For example, for a project on
  implementing sorting algorithms:
```java
@GradeProject(
    name = "Sorting",
    description = "Implement Many Sorting Algorithms",
    warningPoints = 1.0,
    coveragePoints = 1.0,
    coveragePercentage = 90)
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
  length isn't known until runtime, you *must* specify the number
  of points per test, and you may *optionally* specify a maximum
  number of points for the whole list of tests:
  
```java
  @Grade(project = "RE", topic = "Numbers", points = 1, maxPoints = 5)
  @TestFactory
  List<DynamicTest> testIntegers() { ... }
```
  
## About coverage testing

[JaCoCo](https://www.eclemma.org/jacoco/) has a wide variety of ways that you can configure its Gradle plugin to either
fail or pass the build based on different coverage policies. When we tried to just use
this as-is, students were wildly unhappy with the feedback. The way we do coverage
in RiceChecks is:

- The top-level coverage policy appears in the `GradeProject` annotation.
  - With `coveragePoints = N`
you specify the number of points (all or nothing) for satisfying the coverage policy.
  - You specify a `coveragePercentage` you wish to require of every covered class.
  - You may optionally specify a `coverageStyle`, and your choices are `LINES` or `INSTRUCTIONS`,
    corresponding to the same terms as JaCoCo understands them. If you're
    concerned that students might try to mash too much code onto a
    single line in order to thwart line-counting coverage, you might
    prefer the `INSTRUCTIONS` mode, which is based on Java bytecode operations,
    or you could require GoogleJavaFormat, described above, which would
    reject such shenanigans.

- You annotate packages (via `package-info.java`) or classes with an `@GradeCoverage`
  annotation to note which project(s) care about coverage for those classes or packages.
```java
@GradeCoverage(project = "Sorting")
public class HeapSort { ... }
```
- You can set an `exclude = true` flag on the `GradeCoverage` annotation if you want to say that a particular class is
  *not* to be considered for coverage testing. This might make sense if
  you've enabled coverage testing on an entire package but wish to
  exclude a specific class within the package. A `GradeCoverage` annotation
  applies recursively to inner classes as well as the annotated class.
  If there are multiple applicable coverage annotations external to a class, the closest one
  wins.
  
- As part of your `@GradeProject` annotation you specify how many points are awarded
  for meeting the coverage requirement. It's all or nothing. You can also specify
  whether coverage is evaluated by `LINE` or `INSTRUCTION`.
- Each class (or inner class) is evaluated for its coverage, for the desired metric,
  independently. Every class must pass for RiceChecks to grant the coverage points.
  
## Integrating the autograder into your Gradle projects

- There are three different Jar files (built by the `gradlew allJars` task):
  - `RiceChecks-0.1.jar` -- a "thin" Jar file, including the annotations and the autograder, but
    without its external dependencies. Suitable for executing from a Gradle environment, which will
    recursively fetch any dependencies.
  - `RiceChecks-fat-0.1.jar` -- a "fat" Jar file, including the annotations, the autograder, and
    *all of the recursive dependencies of the autograder*. If you want to be able to run the autograder
    directly from the command-line (e.g., `java -jar RiceChecks-fat-0.1.jar --project p1 grade`),
    then this Jar file has everything necessary.
  - `AnnotationAutoGrader-annotations-0.1.jar` -- a tiny Jar file, including *only* the annotations
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
  Whenever somebody comes out with a clever Java processing tool, they generally
  release a Gradle plugin for it.

- **Why do you write out the grading policy to a YAML file? Why not just
  re-read the annotations every time?** Let's say you want to have "secret" unit
  tests that you don't initially give to your students. You can construct a policy
  with them present, save it to the YAML file, and then delete the file prior
  to distributing the project to your students. When the student runs the autograder,
  it will notice that the "secret" tests are missing and treat them as having failed.
  When you later add them back in, everything works.
  
- **Why did you write the autograder itself in Kotlin as opposed to ...?** One main
  motivating factor in the RiceChecks implementation was leveraging the efforts
  of the [ClassGraph](https://github.com/classgraph/classgraph) project, which makes it
  straightforward to extract all the annotations. Since it's a Java library, we could
  call it from Java, Kotlin, Scala, or any other JVM language. Otherwise, there's
  nothing we're doing in Kotlin that we couldn't do in vanilla Java,
  except Kotlin is both more concise than Java while also being *readable* to 
  most Java developers.
  
- **Can I hack together machine-readable output from RiceChecks / Can I hack RiceChecks to
  send grades automatically to my server?** Have a look at [Aggregators.kt](blob/master/autograder/src/main/kotlin/edu/rice/autograder/Aggregators.kt),
  which collects together a `List<EvaluatorResult>` and prints it. You'd
  instead want to convert that list to your favorite format and then operate on it.
  We decided not to do this because we wanted to have human graders in the
  loop for things that we cannot automatically check (e.g., whether a design
  is "good") and to make sure that students weren't doing something undesirable,
  like editing our provided unit tests.
