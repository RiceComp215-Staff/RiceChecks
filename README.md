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
you can `gradlew jar` to make the jar files needed to install AnnoAutoGrader elsewhere, 
or you can use `gradlew demoSetup` to copy the relevant files into the demo projects.

(At least for now, we're not pushing these Jar files to any sort of MavenCentral server, although
this would be easy enough to do.)

## Integrating the autograder into your Gradle projects

- Run `gradlew jar` to generate the Jar file (or download from `link TBD`)
- Place the Jar file into the `libs` directory
- Inside your `build.gradle` file:
  - Add a `libs` line into `dependencies`
  - Add assorted rules to run the tests you care about, but you don't want them to fail the build.
  - Perhaps better run as actions by Travis-CI or equivalent.
    - Example
    - Example
    - Example
  - Then, add Gradle code to execute the autograder, which reads the XML files left behind by the previous actions.
    - Example
    - Example
    - Example
  - Run the TBD extractor action to extract a grading policy from your code annotations
    - Examine the policy to see if it matches your intuition on total points and other such things. If not, edit your annotations and repeat.
      - This solves a significant paint point with other autograders: making it easy to keep
        the grade specs and the code specs in alignment. You extract a grade spec from your
        code in the same way that JavaDoc extracts documentation specs. 
      - _I guess this means AnnoAutoGrader is a *literate autograder*_. That's kinda cool.
    - Place the resulting `grade-policy.yaml` file in `config`
    - You may now choose to delete one or more of the source files from which the config was generated
    (perhaps because they contained private tests that you don't want the students to see).
    - The autograder, seeing no such tests at runtime, will treat those missing test as failing
