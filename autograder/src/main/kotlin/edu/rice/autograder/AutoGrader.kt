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
// https://stackoverflow.com/questions/7111362/pulling-a-gradle-dependency-jar-from-maven-and-then-running-it-directly

// TODO: evaluate rules against results!
//   TODO: GoogleJavaStyle
//   TODO: CheckStyle
//   TODO: Jacoco
//   TODO: JUnit
//   TODO: Compiler warnings/ErrorProne

enum class Task {
    debugAnnotations, writeConfig, grade
}

private const val TAG = "GradleResultScanner"

object AutoGrader {
    @JvmField
    @Parameter(names = ["--package"], description = "Java package where student code can be found (only used when reading annotations from student code)")
    var packageName: String? = null

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
    @Parameter(names = ["--build-dir"], description = "Build directory")
    var buildDir: String = "./build"

    @JvmField
    @Parameter(description = "task")
    var taskString: String = "grade"

    var task = Task.grade // will change below with arg parsing

    private lateinit var commandParser: JCommander

    private fun helpDump() {
        commandParser.usage()
        System.out.print("\nThree tasks are supported:\n" +
                ". debugAnnotations: Reads all the grading annotations and prints their interpretation\n" +
                "      for the requested project. Requires a --package annotation.\n" +
                "\n" +
                ". writeConfig: Reads all the grading annotations and writes a YAML config file\n" +
                "      for the requested project to the filename specified by the --config parameter." +
                "      Also requires a --package argument.\n" +
                "\n" +
                ". grade: The default task, loads the autograder spec for the requested project.\n" +
                "      --config can be used to specify a YAML file for the project autograde spec, \n" +
                "      or, by default, the autograde spec is loaded from the code annotations, which" +
                "      requires a --package argument.\n")
        System.exit(1)
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

        // We're making "local" copies of the properties so that Kotlin's nullity inferences work
        val lConfigFileName = configFileName
        val lProject = project
        val lPackageName = packageName

        when (task) {
            Task.debugAnnotations -> {
                if (lProject != null && lPackageName != null) {
                    Log.i(TAG, "scanning package($lPackageName), for project($lProject)")
                    val gproject = scanEverything(lPackageName)[lProject]
                    if (gproject == null) {
                        Log.e(TAG, "No annotations found for project($lProject)")
                        System.out.println("No annotations found for project($lProject)")
                        System.exit(1)
                    } else {
                        System.out.println(gproject.yamlExporter())
                        System.exit(0)
                    }
                } else {
                    helpDump()
                }
            }

            Task.writeConfig ->
                if (lConfigFileName != null && lProject != null && lPackageName != null) {
                    Log.i(TAG, "scanning package($lPackageName), writing configuration for project($lProject) to $lConfigFileName")
                    val gproject = scanEverything(lPackageName)[lProject]
                    if (gproject == null) {
                        Log.e(TAG, "No annotations found for project($lProject)")
                        System.out.println("No annotations found for project($lProject)")
                        System.exit(1)
                    } else {
                        writeFile(lConfigFileName, gproject.yamlExporter())
                                .onSuccess {
                                    System.out.println("Config for $lProject written to $lConfigFileName")
                                    System.exit(0)
                                }
                                .onFailure {
                                    System.out.println("Error writing to $lConfigFileName: ${it.message}")
                                    System.exit(1)
                                }
                    }
                } else {
                    helpDump()
                }

            Task.grade -> when {
                lConfigFileName != null && lProject != null -> {
                    Log.i(TAG, "Running autograder with configFileName($lConfigFileName), project($lProject)")
                    val gproject = loadConfig(lConfigFileName)
                    val passed = gproject.printResults(System.out, gproject.allResults())
                    System.exit(if (passed) 0 else 1)
                }
                lConfigFileName == null && lProject != null && lPackageName != null -> {
                    Log.i(TAG, "Running autograder from annotations for package($lPackageName), project($lProject)")
                    val gproject = scanEverything(lPackageName)[lProject]
                    if (gproject == null) {
                        Log.e(TAG, "No annotations found for project($lProject)")
                        System.out.println("No annotations found for project($lProject)")
                        System.exit(1)
                    } else {
                        val passed = gproject.printResults(System.out, gproject.allResults())
                        System.exit(if (passed) 0 else 1)
                    }
                }
                else -> helpDump()
            }
        }
    }
}

fun loadConfig(yamlFileName: String): GGradeProject =
    readFile(yamlFileName)
            .flatMap { yamlImporter(it) }
            .onFailure {
                Log.e(TAG, "Failed to load $yamlFileName", it)
                System.out.println("Failed to load $yamlFileName: ${it.message}")
                System.exit(1)
            }
            .getOrFail()

/** Entry point for calling the autograder from the command-line. */
fun main(args: Array<String>) {
    AutoGrader.autoGrade(args)
}
