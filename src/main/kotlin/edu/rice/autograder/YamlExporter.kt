package edu.rice.autograder

import kotlin.system.exitProcess

/**
 * Given a project name, as specified with a [GradeProject] annotation,
 * returns a String corresponding to a YAML configuration suitable
 * for our autograder to evaluate the score.
 */
fun yamlExporter(projectName: String, codePackage: String): String {
    val stringBuffer = StringBuilder()
    val scan = scanEverything(codePackage)
    val project = scan[projectName]
    if (project == null) {
        System.err.println("Unknown project: $projectName")
        exitProcess(1)
    }

    stringBuffer.append("name: \"${project.name}\"\n" +
            "totalScore: ${project.maxPoints}\n" +
            "vcs:\n" +
            "  git: true\n" +
            "students:\n" +
            "  location: README.md\n" +
            "  require: true\n" +
            "notes:\n" +
            "  \"${project.description}\"\n" +
            "secure: config/security.policy\n" +
            "reporting:\n" +
            "  default: file\n" +
            "  used: file\n" +
            "  file: build/reports/grade.json\n")

    if (project.warningPoints > 0.0)
        stringBuffer.append("errorprone:\n" +
                "  name: \"Compilation\"\n" +
                "  points: ${project.warningPoints}\n" +
                "  message: \"Compilation or errorprone warnings found\"\n" +
                "checkstyle:\n" +
                "  name: \"checkstyle Tests\"\n" +
                "  missing:\n" +
                "    points: ${project.warningPoints}\n" +
                "    message: \"checkstyle failed to run\"\n" +
                "    name: \"checkstyle\"\n" +
                "  selectors:\n" +
                "    - selector: \"count(//file/error) = 0\"\n" +
                "      points: 0\n" +
                "      message: \"No checkstyle errors were reported\"\n" +
                "      name: \"checkstyle\"\n" +
                "    - selector: \"count(//file/error) > 0\"\n" +
                "      points: ${project.warningPoints}\n" +
                "      message: \"checkstyle errors were reported\"\n" +
                "      name: \"checkstyle\"\n")

    if (project.coveragePoints > 0.0 && project.coverageClasses.isNotEmpty()) {
        stringBuffer.append("jacoco:\n" +
                "  enabled: true\n" +
                "  name: \"Jacoco Tests\"\n" +
                "  coverageRatio: ${project.coverageRatio}\n" +
                "  points: ${project.coveragePoints}\n" +
                "  rule: \"${project.coverageMethod}\"\n"  +
                "  classes:\n")
        project.coverageClasses.map { "      - '${it}'\n" }.forEach { stringBuffer.append(it) }
    } else {
        stringBuffer.append("jacoco:\n" +
                "  enabled: false\n")
    }

    stringBuffer.append("tests:\n")
    project.topics.forEach { topic ->
        stringBuffer.append(
                " - topic: \"${topic.name}\"\n" +
                "   points: ${topic.maxPoints}\n" +
                "   selectors:\n")
        topic.tests.forEach { test ->
            val shortClassName = test.className.split('.').last()
            stringBuffer.append("    - name: \"${shortClassName}\"\n")
            if (test.testFactory) {
                stringBuffer.append(
                        "      isDynamic: true\n" +
                        "      totalSelector: \"count(//testsuite[@name='${test.className}']//testcase[starts-with(@name,'${test.methodName}()')])\"\n" +
                        "      failedSelector: \"count(//testsuite[@name='${test.className}']//testcase[starts-with(@name,'${test.methodName}()')]//failure)\"\n" +
                        "      message: \"test failure(s) in ${test.methodName}\"\n" +
                        "      pointsPerFailure: ${test.points}\n" +
                        "      totalPoints: ${test.maxPoints}\n")
            } else {
                stringBuffer.append(
                        "      selector: \"count(//testsuite[@name='{${test.className}']) = 1 and count(//testcase[@name='${test.methodName}()']/failure) = 1\"\n" +
                        "      points: ${test.points}\n" +
                        "      message: \"Failure in ${test.methodName}\"\n")
            }
        }
        // TODO: Output one per class, to indicate no failures in that class
        //   - selector: "count(//testsuite[@name='edu.rice.rpn.RPNCalculatorTestPrivate']) = 1 and count(//testsuite[@name='edu.rice.rpn.RPNCalculatorTestPrivate']//failure) = 0"
        //    score: 0
        //    message: "No test failures in RPNCalculatorTestPrivate"
        //    name: "RPNCalculatorTestPrivate"

        // TODO: Output one per class, to indicate class wasn't there at all, no points
        //   - selector: "count(//testsuite[@name='edu.rice.rpn.RPNCalculatorTestPrivate']) = 0"
        //    score: 3
        //    message: "RPNCalculatorTestPrivate didn't compile"
        //    name: "RPNCalculatorTestPrivate"

        // TODO: what do we want to do if multiple topics share tests in a given class?
        // We want to have a better way of dealing with the file-didn't-compile-or-is-missing case
    }

    return stringBuffer.toString()
}

