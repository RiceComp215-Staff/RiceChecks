//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import arrow.core.Try
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter

// TODO: figure out whether need "thin" vs. "fat" Jars and how to distribute
// https://stackoverflow.com/questions/29643973/run-jar-with-parameters-in-gradle
// Or, we could just stuff the generated Jar file into the GitHub repository

// TODO: evaluate rules against results!
//   TODO: GoogleJavaStyle
//   TODO: CheckStyle
//   TODO: Jacoco
//   TODO: JUnit
//   TODO: Compiler warnings/ErrorProne

internal enum class Task {
    debugAnnotations, writeConfig, grade
}

private const val TAG = "GradleResultScanner"

internal object GradleResultScanner {
    @JvmField
    @Parameter(names = ["--project"], description = "Name of the project to be graded (required)", required = true)
    var project: String? = null

    @JvmField
    @Parameter(names = ["--config"], description = "Name of the YAML-formatted configuration file")
    var configFileName: String? = null

    @JvmField
    @Parameter(names = ["--help"], description = "Prints usage information", help = true)
    var help: Boolean = false

    @JvmField
    @Parameter(names = ["--log"], description = "Internal logging level (one of: all, error, nothing)")
    var logString: String = "nothing"

    @JvmField
    @Parameter(description = "task")
    var taskString: String = "grade"

    var task = Task.grade

    lateinit var commandParser: JCommander

    fun helpDump() {
        commandParser.usage()
        System.out.print("\nThree tasks are supported:\n" +
                ". debugAnnotations: Reads all the grading annotations and prints their interpretation\n" +
                "      for the requested project.\n" +
                "\n" +
                ". writeConfig: Reads all the grading annotations and writes a YAML config file\n" +
                "      for the requested project to the filename specified by the --config parameter.\n" +
                "\n" +
                ". grade: The default task, loads the autograder spec for the requested project.\n" +
                "      --config can be used to specify a YAML file for the project autograde spec, \n" +
                "      or, by default, the autograde spec is loaded from the code annotations\n")
        System.exit(0)
    }

    fun autoGrade(args: Array<String>) {
        commandParser = JCommander.newBuilder()
                .addObject(this)
                .build()

        Try { commandParser.parse(*args) }.onFailure {
            System.out.println(it.message)
            helpDump()
        }

        if (help) helpDump()
        task = Try { enumValueOf<Task>(taskString) }
                .onFailure { helpDump() }
                .getOrFail()

        Try { Log.setLogLevel(logString) }
                .onFailure { helpDump() }

        Log.i(TAG, "Starting GradleResultScanner for $task")
        Log.logProperties()
        Log.i(TAG, "project: $project")
        Log.i(TAG, "configFileName: $configFileName")
        Log.i(TAG, "task: $task")

        when (task) {
            Task.debugAnnotations -> System.out.println("debugAnnotations")
            Task.writeConfig -> System.out.println("writeConfig")
            Task.grade -> System.out.println("grade")
        }
    }
}

/** Entry point for calling the autograder from the command-line. */
fun main(args: Array<String>) {
    GradleResultScanner.autoGrade(args)
}
