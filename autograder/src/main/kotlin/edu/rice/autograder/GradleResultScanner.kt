//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

// TODO: how to manage annotations on outer classes and inner classes
// The inner annotations should override the outer annnotations!?

// TODO: figure out how to make a "fat Jar" file, so it can go online and be distributed in "compiled" form
// (Or, how to use the 'application' plugin, which also knows how to do this.)
// https://discuss.gradle.org/t/how-to-include-dependencies-in-jar/19571
// https://www.mkyong.com/gradle/gradle-create-a-jar-file-with-dependencies/
// https://www.baeldung.com/gradle-fat-jar
// https://stackoverflow.com/questions/21721119/creating-runnable-jar-with-gradle
// https://stackoverflow.com/questions/20728621/running-an-executable-jar-file-built-from-a-gradle-based-project

// TODO: figure out how to download and execute that Jar as a separate thing
// https://stackoverflow.com/questions/29643973/run-jar-with-parameters-in-gradle
// Or, we could just stuff the generated Jar file into the GitHub repository

// TODO: figure out where compiler warnings / errors go (not in build directory?!)

// TODO: evaluate rules against results!
//   TODO: GoogleJavaStyle
//   TODO: CheckStyle
//   TODO: Jacoco
//   TODO: JUnit
//   TODO: Compiler warnings/ErrorProne
