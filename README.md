# RiceChecks
This project defines an autograder for Java and Gradle-based student projects. As an instructor, you *annotate* your unit tests
to say how many points each is worth. These and other annotations are processed by the Java compiler into the resulting
class files.
 
The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your master branch.
- You extract a *grading policy* for every given project, which you can then include in the `config` directory
  that is handed out to your students
  - This policy is a human-readable YAML file, so you can easily review it, for example, to
    ensure that you have the expected total number of points.
- You provide your students with a `build.gradle` file, specifying all the actions that need to
  be run. This includes JUnit4/5 tests, CheckStyle, google-java-style, ErrorProne, and JaCoCo.
- You add the autograder code to `build.gradle` that we provide, which runs all these things,
  writing their output to files, and in some cases like the unit tests, making sure that a 
  failure of one doesn't preclude the rest of the tests from running. The autograder runs
  as a standalone Java program, reading all this output and evaluating it with respect
  to the grading policy, printing its results to standard output, and then it either
  exits with 0 or non-zero (success or failure), giving the student a "green check" or "red X" 
  on GitHub.
- You configure your CI service to run all of this every time there's a push to GitHub.
- We provide you several example projects so you can see how this all fits together.
    
RiceChecks, itself, is written in Kotlin, and should be able to process student projects written in Java or Kotlin,
although our focus is on Java-based student projects, at least for now. Notably, RiceChecks
runs as a standalone Java program, reading the various log files left behind as part of Gradle
compiling and testing a program. It writes its grading conclusions to standard-out as human-readable
text. Our human graders read this text from the CI system's logs, and then transcribe these
numbers into our university learning management system (Canvas), while also looking over the
student projects to ensure that they didn't do something sketchy.

**RiceChecks, itself, is only tested against OpenJDK 11. It's unlikely to work on earlier JDK releases.
Student code running with earlier Java releases should work just fine, although we do everything on the same version.**


## Compiling / developing for RiceChecks
The `autograder` directory includes our autograder implementation (in Kotlin) and annotations (Java)
as well as a pile of unit tests. The other top-level directories are demonstration projects. 

You can run `gradlew check` or `gradlew test` to run the unit tests in the `autograder` project, 
you can `gradlew allJars` to make the jar files needed to install RiceChecks elsewhere, 
or you can use `gradlew demoSetup` to copy the relevant files into the demo projects.

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

- Our sample gradle projects use `RiceChecks-annotations-0.1.jar` as a dependency for
  student code, and from Gradle will invoke the main autograder in `RiceChecks-0.1.jar`,
  recursively pulling in the necessary dependencies. (Notably, we don't want to pollute
  students' code namespaces with symbols from the autograder.)
  
- Ultimately, the `gradlew autograde` action replaces what might normally be a call to
  `gradlew check`. The `autograde` action runs the compiler and lint checks, unit tests, 
  and coverage tests, then prints its summary and either passes or fails the build based
  on whether any deductions were found.

## About coverage testing

JaCoCo has a wide variety of ways that you can configure its Gradle plugin to either
fail or pass the build based on different coverage policies. When we tried to just use
this as-is, students were wildly unhappy with the feedback. The way we do coverage
in RiceChecks is:
- Annotate packages (via `package-info.java`) or classes with an `@GradeCoverage`
  annotation to note which project(s) care about coverage for those classes or packages.
- You can set an `exclude = true` flag if you want to say that a particular class is
  *not* to be considered for coverage testing.
- As part of your `@GradeProject` annotation you specify how many points are awarded
  for meeting the coverage requirement. It's all or nothing. You can also specify
  whether coverage is evaluated by `LINE` or `INSTRUCTION`.
- Each class (or inner class) is evaluated for its coverage, for the desired metric,
  independently. Every class must pass for RiceChecks to grant the coverage points.
