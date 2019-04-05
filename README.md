# AnnoAutoGrader
This project defines an autograder for Java and Gradle-based student projects. As an instructor, you *annotate* your unit tests
to say how many points each is worth. These and other annotations are processed by the Java compiler into the resulting
class files.
 
The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your code.
- You extract a *grading policy* for every given project, which you can then include in the `config` directory
  that is handed out to your students
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
    
AnnoAutoGrader, itself, is written in Kotlin, and should be able to process student projects written in Java or Kotlin,
although our focus is on Java-based student projects, at least for now.

## Compiling / developing for AnnoAutoGrader
The `autograder` directory includes our autograder implementation (in Kotlin) and annotations (Java)
as well as a pile of unit tests. The other top-level directories are demonstration projects. 

You can run `gradlew check` or `gradlew test` to run the unit tests in the `autograder` project, 
you can `gradlew allJars` to make the jar files needed to install AnnoAutoGrader elsewhere, 
or you can use `gradlew demoSetup` to copy the relevant files into the demo projects.

## Integrating the autograder into your Gradle projects

- There are three different Jar files (built by the `gradlew allJars` task):
  - `AnnoAutoGrader-0.1.jar` -- a "thin" Jar file, including the annotations and the autograder, but
    without its external dependencies. You might include this in the "libs" directory of a Gradle
    project or include it from a Maven server (TBD).
  - `AnnoAutoGrader-fat-0.1.jar` -- a "fat" Jar file, including the annotations, the autograder, and
    *all of the recursive dependencies of the autograder*. If you want to be able to run the autograder
    directly from the command-line (e.g., `java -jar AnnoAutoGrader-fat-0.1.jar --project p1 grade`),
    then this Jar file has everything necessary.
  - `AnnotationAutoGrader-annotations-0.1.jar` -- a tiny Jar file, including *only* the annotations
    and nothing else. This is the only dependency you want to be visible to student projects,
    so they don't accidentally start calling into other autograder functions.

- Our sample gradle files use `AnnotationAutoGrader-annotations-0.1.jar` as a dependency for
  student code, and from Gradle will invoke the main autograder library
  - Add appropriate tasks and dependencies to build.gradle
    - Task to extract autograder policy to yaml file
    - Task to run autograder based on yaml file
    - Various changes to build.gradle so the build doesn't stop on first failure

  - Project dev pipeline:
    - Check out your project-specific branch
      - All our projects are branches from "master"
      - We then delete everything from future projects (in the branch)
      - This leaves a branch with the reference solution intact
    - Set build.gradle variable with project name
    - While your reference solution is still there, extract the autograder policy
    - Verify that running `gradlew autograde` yields perfect scores
    - Modify files / remove reference solution
    - Verify that running `gradlew autograde` yields the correct minimum score
    
