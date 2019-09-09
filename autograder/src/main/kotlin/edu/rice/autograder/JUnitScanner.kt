/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Date
import kotlin.math.max
import kotlin.math.min

// A successful unit test file, with a name like TEST-edu.rice.json.ParserText.xml, looks like this:
//
// <?xml version="1.0" encoding="UTF-8"?>
// <testsuite name="edu.rice.json.ParserTest" tests="11" skipped="0" failures="0" errors="0" timestamp="2019-03-25T15:11:16" hostname="bunsen-honeydew.cs.rice.edu" time="0.016">
// <properties/>
// <testcase name="buildersEquivalentToParser()" classname="edu.rice.json.ParserTest" time="0.0"/>
// <testcase name="expectedIndentation()" classname="edu.rice.json.ParserTest" time="0.0"/>
// <testcase name="jsonObjectsParseCorrectly()" classname="edu.rice.json.ParserTest" time="0.001"/>
// <testcase name="successfulParseOfBasicObject()" classname="edu.rice.json.ParserTest" time="0.001"/>
// <testcase name="simpleParserNullProductionTest()" classname="edu.rice.json.ParserTest" time="0.001"/>
// <testcase name="failedParseOfCorruptBasicObject()" classname="edu.rice.json.ParserTest" time="0.001"/>
// <testcase name="successfulParseOfBigObject()" classname="edu.rice.json.ParserTest" time="0.005"/>
// ...
// <system-out> .... string output ... </system.out>
// <system-err> .... string output ... </system.err>
// </testsuite>

// If it was a JUnit5 TestFactory, it will look more like this:
// <?xml version="1.0" encoding="UTF-8"?>
// <testsuite name="edu.rice.json.ParserTestPrivate" tests="435" skipped="0" failures="0" errors="0" timestamp="2019-03-25T15:11:16" hostname="bunsen-honeydew.cs.rice.edu" time="0.103">
// <properties/>
// <testcase name="testObjects()[1]" classname="edu.rice.json.ParserTestPrivate" time="0.001"/>
// <testcase name="testObjects()[2]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[3]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[4]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[5]" classname="edu.rice.json.ParserTestPrivate" time="0.001"/>
// <testcase name="testObjects()[6]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[7]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[8]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[9]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[10]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[11]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[12]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[13]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[14]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// <testcase name="testObjects()[15]" classname="edu.rice.json.ParserTestPrivate" time="0.0"/>
// ...

// And if a testcase fails, it looks like this:

//  <testcase name="testSuite()[22]" classname="edu.rice.tree.TreapTest" time="0.026">
//    <failure message="org.opentest4j.AssertionFailedError" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError
//        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:35)
//        at org.junit.jupiter.api.Assertions.fail(Assertions.java:98)
//        at edu.rice.tree.TreeSuite.toListIsSorted(TreeSuite.java:554)
//        at org.junit.jupiter.engine.descriptor.JupiterTestDescriptor.executeAndMaskThrowable(JupiterTestDescriptor.java:204)
//        at org.junit.jupiter.engine.descriptor.DynamicTestTestDescriptor.execute(DynamicTestTestDescriptor.java:43)
//        at org.junit.jupiter.engine.descriptor.DynamicTestTestDescriptor.execute(DynamicTestTestDescriptor.java:25)
//        at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:135)
//        ...
//    </failure>
// </testcase>

// So, the factory tests have method names with an index number afterward, but otherwise look just like regular tests.

@JsonRootName("testsuite")
data class JUnitSuite(
    @set:JsonProperty("testcase")
    var tests: List<JTestCase>? = null,

    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var className: String? = null,

    @set:JacksonXmlProperty(localName = "tests", isAttribute = true)
    var numTests: Int = 0,

    @set:JacksonXmlProperty(localName = "skipped", isAttribute = true)
    var numSkipped: Int = 0,

    @set:JacksonXmlProperty(localName = "failures", isAttribute = true)
    var numFailures: Int = 0,

    @set:JacksonXmlProperty(localName = "errors", isAttribute = true)
    var numErrors: Int = 0,

    @set:JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    var timeStamp: Date? = null,

    @set:JacksonXmlProperty(localName = "hostname", isAttribute = true)
    var hostName: String? = null,

    @set:JacksonXmlProperty(localName = "time", isAttribute = true)
    var duration: Double = 0.0
)

@JsonRootName("testcase")
data class JTestCase(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var methodName: String? = null,

    @set:JacksonXmlProperty(localName = "classname", isAttribute = true)
    var className: String? = null,

    @set:JacksonXmlProperty(localName = "time", isAttribute = true)
    var duration: Double = 0.0,

    @set:JsonProperty("failure")
    var failure: JFailure? = null
)

@JsonRootName("failure")
data class JFailure(
    @set:JacksonXmlProperty(localName = "message", isAttribute = true)
    var message: String? = null,

    @set:JacksonXmlProperty(localName = "type", isAttribute = true)
    var type: String? = null
) {
    // Jackson workaround: https://github.com/FasterXML/jackson-module-kotlin/issues/138
    @JacksonXmlText
    var stackTrace: String? = null

    /** Converts the stack backtrace to a list of strings starting at the frame where the exception was thrown. */
    val stackTraceList: List<String> by lazy {
        stackTrace?.split(Regex("[\n\r]+\\s*")) ?: emptyList()
    }
}

/**
 * Given a string -- the result of reading a JUnit XML results file --
 * returns a [JUnitSuite] data class, suitable for subsequent queries.
 */
fun junitSuiteParser(fileData: String): JUnitSuite = jacksonXmlMapper.readValue(fileData)

fun JTestCase.matches(className: String, methodName: String): Boolean {
    val fixedMethodName = this.methodName?.replace(Regex("\\(\\).*$"), "") ?: ""
    return fixedMethodName == methodName && this.className == className
}

private fun List<JUnitSuite>.find(className: String, methodName: String): List<JTestCase> =
    flatMap { suite ->
        suite.tests
            ?.filter { it.matches(className, methodName) }
            ?: emptyList()
    }

/**
 * Returns a list of [EvaluatorResult] -- one per [GGradeTopic] associated with
 * the given [GGradeProject].
 */
fun List<JUnitSuite>.eval(project: GGradeProject): List<EvaluatorResult> =
    project.topics.map { (topicName, topicMaxPoints, tests) ->
        // - Each "topic" in a given project maps to one EvaluatorResult.
        // - Each EvaluatorResult has all the points worked out and then
        //   a list of UnitTestDeductions.
        val topicResults = tests.map { (points, maxPoints, className, methodName, testFactory) ->
            val name = "$className.$methodName"
            val testResults = find(className, methodName)

            when {
                testFactory && testResults.isEmpty() ->
                    UnitTestFactoryDeduction("$name: missing", maxPoints, name, 0, 0)

                testResults.isEmpty() -> UnitTestDeduction("$name: missing", points, name)

                testFactory -> {
                    val numPassing = testResults.count { it.failure == null }
                    val numFailing = testResults.count { it.failure != null }
                    val totalTests = testResults.size
                    val deduction = min(numFailing * points, maxPoints)

                    UnitTestFactoryDeduction(
                        "$name:\n$numPassing of $totalTests passing (-%.1f / fail)".format(points),
                        deduction, name, numPassing, totalTests
                    )
                }

                testResults.find { it.failure != null } != null ->
                    UnitTestDeduction("$name: failed", points, name)

                else -> UnitTestDeduction("$name: passed", 0.0, name)
            }
        }

        val numTests = topicResults.size
        val numPassingTests = topicResults.count { it.cost == 0.0 }

        val topicDeductions = min(topicResults.sumByDouble { it.cost }, topicMaxPoints)
        val topicString = "$topicName: $numPassingTests of $numTests tests passed"

        EvaluatorResult(
            topicDeductions == 0.0, max(topicMaxPoints - topicDeductions, 0.0),
            topicMaxPoints, topicString, TESTS_CATEGORY, topicResults
        )
    }
