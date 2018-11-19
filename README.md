# AnnoAutoGrader
This project defines an autograder for Java and Gradle-based student projects. As an instructor, you *annotate* your unit tests
to say how many points each is worth. These and other annotations are processed by the Java compiler into the resulting
class files. The autograder then reads in the annotations as well as all the XML documents written out as Gradle compiles
the program, runs the tests, and executes the JaCoCo coverage tool.

The autograder can be integrated into any Gradle build file simply:
`TBD`

Ultimately, it's just a program that exits with a zero for success and a one for failure (standard Unix practice), so
you can make sure that your build fails when the autograder fails.

Probably the easiest way to get started is to rummage around the [autogradertest package](tree/master/src/test/java/edu/rice/autogradertest) to see
how you can annotate your unit tests.

AnnoAutoGrader, itself, is written in Kotlin, and should be able to process student projects written in Java or Kotlin,
although our focus is on Java-based student projects, at least for now.
