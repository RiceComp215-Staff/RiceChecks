/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.Try
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import kotlin.system.exitProcess

enum class Task {
    debugAnnotations, writeConfig, grade
}

private const val TAG = "GradleResultScanner"
const val AutoGraderName = "RiceChecks"

object AutoGrader {
    // All these annotations are really configuration parameters for JCommander,
    // which parses the command-line arguments and fills out the variables.
    @JvmField
    @Parameter(names = ["--package"],
        description = "Java package name for student code (ignored if --config is specified)")
    var packageName: String? = null

    @JvmField
    @Parameter(names = ["--project"],
        description = "Name of the project to be graded (required)", required = true)
    var project: String? = null

    @JvmField
    @Parameter(names = ["--config"],
        description = "Name of the YAML-formatted configuration file")
    var configFileName: String? = null

    @JvmField
    @Parameter(names = ["--help"],
        description = "Prints usage information", help = true)
    var help: Boolean = false

    @JvmField
    @Parameter(names = ["--log"],
        description = "Internal logging level (one of: all, error, nothing)")
    var logString: String = "nothing"

    @JvmField
    @Parameter(names = ["--build-dir"], description = "Build directory")
    var buildDir: String = "./build"

    @JvmField
    @Parameter(description = "task")
    var taskString: String = "grade"

    var task = Task.grade // will change below with arg parsing

    private lateinit var commandParser: JCommander

    private fun helpDumpAndExit() {
        commandParser.usage()
        System.out.print("\n$AutoGraderName supports these tasks:\n" +
                ". debugAnnotations: Reads all the grading annotations and prints their\n" +
                "      interpretation for the requested project. Requires a --package " +
                "      annotation.\n" +
                "\n" +
                ". writeConfig: Reads all the grading annotations and writes a YAML config\n" +
                "      file for the requested project to the filename specified by the\n" +
                "      --config parameter. Also requires a --package argument.\n" +
                "\n" +
                ". grade: The default task, loads the autograder spec for the requested\n" +
                "      project. --config can be used to specify a YAML file for the project\n" +
                "      autograde spec, or, by default, the autograde spec is loaded from the\n" +
                "      code annotations, which requires a --package argument.\n")
        exit(false)
    }

    private fun exit(passing: Boolean): Nothing = exitProcess(if (passing) 0 else 1)

    fun autoGrade(args: Array<String>) {
        commandParser = JCommander.newBuilder()
                .addObject(this)
                .build()

        Try { commandParser.parse(*args) }.onFailure {
            System.out.println(it.message)
            helpDumpAndExit()
        }

        if (help) helpDumpAndExit()

        task = Try { enumValueOf<Task>(taskString) }
                .onFailure { helpDumpAndExit() }
                .getOrFail()

        Try { Log.setLogLevel(logString) }
                .onFailure { helpDumpAndExit() }

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
                        exit(false)
                    } else {
                        System.out.println(gproject.yamlExporter(true))
                        exit(true)
                    }
                } else {
                    helpDumpAndExit()
                }
            }

            Task.writeConfig ->
                if (lConfigFileName != null && lProject != null && lPackageName != null) {
                    Log.i(TAG, "scanning package($lPackageName), writing configuration for " +
                        "project($lProject) to $lConfigFileName")
                    val gproject = scanEverything(lPackageName)[lProject]
                    if (gproject == null) {
                        Log.e(TAG, "No annotations found for project($lProject)")
                        System.out.println("No annotations found for project($lProject)")
                        exit(false)
                    } else {
                        writeFile(lConfigFileName, gproject.yamlExporter())
                                .onSuccess {
                                    System.out.println("Config for $lProject written to " +
                                        "$lConfigFileName")
                                    exit(true)
                                }
                                .onFailure {
                                    System.out.println("Error writing to $lConfigFileName: " +
                                        "${it.message}")
                                    exit(false)
                                }
                    }
                } else {
                    helpDumpAndExit()
                }

            Task.grade -> when {
                lConfigFileName != null && lPackageName != null -> {
                    System.out.println("Please specify either --config or --package, but not both")
                    helpDumpAndExit()
                }

                lConfigFileName != null && lProject != null -> {
                    Log.i(TAG, "Running autograder with " +
                        "configFileName($lConfigFileName), project($lProject)")
                    val report = loadConfig(lConfigFileName).toResultsReport()
                    report.print(System.out)
                    exit(report.allPassing)
                }

                lConfigFileName == null && lProject != null && lPackageName != null -> {
                    Log.i(TAG, "Running autograder from annotations for " +
                        "package($lPackageName), project($lProject)")

                    val gproject = scanEverything(lPackageName)[lProject]
                    if (gproject == null) {
                        Log.e(TAG, "No annotations found for project($lProject)")
                        System.out.println("No annotations found for project($lProject)")
                        exit(false)
                    } else {
                        val report = gproject.toResultsReport()
                        report.print(System.out)
                        exit(report.allPassing)
                    }
                }

                else -> helpDumpAndExit()
            }
        }
    }

    fun loadConfig(yamlFileName: String): GGradeProject =
            readFile(yamlFileName)
                    .flatMap { yamlImporter(it) }
                    .onFailure {
                        Log.e(TAG, "Failed to load $yamlFileName", it)
                        System.out.println("Failed to load $yamlFileName: ${it.message}")
                        exit(false)
                    }
                    .getOrFail()
}

/** Entry point for calling the autograder from the command-line. */
fun main(args: Array<String>) {
    AutoGrader.autoGrade(args)
}
