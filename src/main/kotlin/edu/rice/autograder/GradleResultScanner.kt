package edu.rice.autograder

// TODO: make a separate project to factor all of this out of the comp215-code codebase

// TODO: figure out how to make a "fat Jar" file, so it can go online and be distributed in "compiled" form
// (Or, how to use the 'application' plugin, which also knows how to do this.)
// https://www.mkyong.com/gradle/gradle-create-a-jar-file-with-dependencies/
// https://www.baeldung.com/gradle-fat-jar
// https://stackoverflow.com/questions/21721119/creating-runnable-jar-with-gradle
// https://stackoverflow.com/questions/20728621/running-an-executable-jar-file-built-from-a-gradle-based-project

// TODO: figure out how to download and execute that Jar as a separate thing
// https://stackoverflow.com/questions/29643973/run-jar-with-parameters-in-gradle
// Or, we could just stuff the generated Jar file into the GitHub repository


// TODO: we need an XML parsing library
// - https://dom4j.github.io/ -- seems simple and usable

// TODO: read build/reports/checkstyle/*.xml
//   Any lines that start with <error imply a checkstyle failure
//   Example: <error line="24" column="32" severity="warning" message="WhitespaceAround: &apos;=&apos; is not preceded with whitespace." source="com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck"/>

// TODO: figure out where compiler warnings / errors go

// TODO: read build/test-results/test/*.xml
//   Each file starts with:
//     <testsuite name="edu.rice.week9lenses.Week9Lab" tests="1" skipped="0" failures="0" errors="0" timestamp="2018-11-17T17:56:42" hostname="dr-teeth" time="0.005">
//     -- tells us how many tests there were, how many errors/failures (what's the difference?), etc.
//
//   Then we get these:
//     <testcase name="testLenses()" classname="edu.rice.week9lenses.Week9Lab" time="0.005"/>
//     -- one of these for each success
//     <testcase name="testSuite()[1]" classname="edu.rice.tree.TreeTest" time="0.0"/>
//     -- one of these for each @TestFactory success

// TODO: run jacocoTestReport, read build/reports/jacoco/test/jacocoTestReport.xml
//   Monster file, we'll need a real XML parser for this
//   Typical contents look like so:
//       <package name="edu/rice/week2lists">
//        <class name="edu/rice/week2lists/ObjectList$Cons">
//            <method name="&lt;init&gt;" desc="(Ljava/lang/Object;Ledu/rice/week2lists/ObjectList;)V" line="97">
//                <counter type="INSTRUCTION" missed="0" covered="9"/>
//                <counter type="LINE" missed="0" covered="4"/>
//                <counter type="COMPLEXITY" missed="0" covered="1"/>
//                <counter type="METHOD" missed="0" covered="1"/>
//            </method>
//            ...
//            <counter type="INSTRUCTION" missed="268" covered="0"/>
//            <counter type="BRANCH" missed="8" covered="0"/>
//            <counter type="LINE" missed="50" covered="0"/>
//            <counter type="COMPLEXITY" missed="22" covered="0"/>
//            <counter type="METHOD" missed="18" covered="0"/>
//            <counter type="CLASS" missed="1" covered="0"/>
//        </class>

//   Or like this if there's actual coverage:
//
//    <package name="edu/rice/week2lists">
//        <class name="edu/rice/week2lists/ObjectList$Cons">
//            <method name="&lt;init&gt;" desc="(Ljava/lang/Object;Ledu/rice/week2lists/ObjectList;)V" line="97">
//                <counter type="INSTRUCTION" missed="0" covered="9"/>
//                <counter type="LINE" missed="0" covered="4"/>
//                <counter type="COMPLEXITY" missed="0" covered="1"/>
//                <counter type="METHOD" missed="0" covered="1"/>
//            </method>
//            <method name="empty" desc="()Z" line="104">
//                <counter type="INSTRUCTION" missed="0" covered="2"/>
//                <counter type="LINE" missed="0" covered="1"/>
//                <counter type="COMPLEXITY" missed="0" covered="1"/>
//                <counter type="METHOD" missed="0" covered="1"/>
//            </method>
//            <counter type="INSTRUCTION" missed="21" covered="18"/>
//            <counter type="BRANCH" missed="4" covered="0"/>
//            <counter type="LINE" missed="7" covered="6"/>
//            <counter type="COMPLEXITY" missed="5" covered="6"/>
//            <counter type="METHOD" missed="3" covered="6"/>
//            <counter type="CLASS" missed="0" covered="1"/>
//        </class>

//    We probably want to ignore the <method> tags and focus on the counters attached to each class.
//    That jives with how we're doing the annotations. We'll want to fetch the class "counters" and
//    add them all up for all the inner classes / anonymous classes / lambdas.

//    Our @GradeCoverage annotations go on the outer classes. We'll then need to use that as a prefix
//    to make sure we pull in all the inner classes.

// TODO: ultimately convert this into a standalone program that we can call from Gradle
//   Simplifies things immensely with respect to the Gradle plugin and whatnot


