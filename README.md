# AnnoAutoGrader
This project defines an autograder for Java and Gradle-based student projects. As an instructor, you *annotate* your unit tests
to say how many points each is worth. These and other annotations are processed by the Java compiler into the resulting
class files.
 
The essential design of the autograder is:
- You decorate your unit tests with annotations that specify their associated projects and points values
  - If you've got a single master repository for multiple separate projects, you do these annotations once
    and they stay put in your code.
  - You extract a *grading policy* for any given week, which you can then include in the `config` directory
    that is handed out to your students
  - Your students' `build.gradle` file and/or Travis-CI configuration specifies all the actions that need to
    be run. This includes JUnit4/5 tests, CheckStyle, google-java-style, ErrorProne, and JaCoCo.
    
The autograder is run by a separate Gradle action after everything else has been done. It reads all the XML documents
written out as Gradle did the above work, and it computes a final grade based on the grading policy. You might also
configure Travis-CI to pass or fail the build based on whether the autograder gives full points to a student.

AnnoAutoGrader, itself, is written in Kotlin, and should be able to process student projects written in Java or Kotlin,
although our focus is on Java-based student projects, at least for now.

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
    - Place the resulting `grade-policy.json` file in `config`
    - You may now choose to delete one or more of the source files from which the config was generated
    (perhaps because they contained private tests that you don't want the students to see).
    - The autograder, seeing no such tests at runtime, will treat those missing test as failing
