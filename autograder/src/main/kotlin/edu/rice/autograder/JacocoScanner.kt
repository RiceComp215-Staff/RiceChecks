//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.module.kotlin.readValue

// This is easily the most complicated XML we'll have to deal with. The original XML
// is one giant line, so check out the more human-readable jacocoTestReport-Reformatted.xml.

// The structure:
// - the top-level report has a list of packages
//   - each package has a list of classes
//     - each class has a list of methods
//       - each method has a list of counters
//     - each class also has a list of counters
//       - each counter has three attributes: type (string), missed (number), and covered (number)
//   - each package also has a list of sourcefiles
//     - each sourcefile has a list of "lines" (unclear what we're supposed to do with this)
//     - each sourcefile also has a list of counters
//   - each package also has a list of counters
//  - the report also has a list of counters

//
// <report name="comp215-code">
//  <sessioninfo id="bunsen-honeydew.cs.rice.edu-465c9d49" start="1553527264146"
//    dump="1553527285337"/>
//  <package name="edu/rice/web">
//    <class name="edu/rice/web/JavaScriptRepl" sourcefilename="JavaScriptRepl.java">
//      <method name="&lt;init&gt;" desc="()V" line="41">
//        <counter type="INSTRUCTION" missed="3" covered="0"/>
//        <counter type="LINE" missed="1" covered="0"/>
//        <counter type="COMPLEXITY" missed="1" covered="0"/>
//        <counter type="METHOD" missed="1" covered="0"/>
//      </method>
//      <method name="logAndJsonError" desc="(Ljava/lang/String;)Ljava/lang/String;" line="68">
//        <counter type="INSTRUCTION" missed="14" covered="0"/>
//        <counter type="LINE" missed="2" covered="0"/>
//        <counter type="COMPLEXITY" missed="1" covered="0"/>
//        <counter type="METHOD" missed="1" covered="0"/>
//      </method>
//      ...
//      <counter type="INSTRUCTION" missed="208" covered="0"/>
//      <counter type="BRANCH" missed="8" covered="0"/>
//      <counter type="LINE" missed="52" covered="0"/>
//      <counter type="COMPLEXITY" missed="20" covered="0"/>
//      <counter type="METHOD" missed="16" covered="0"/>
//      <counter type="CLASS" missed="1" covered="0"/>
//    </class>

@JsonRootName("report")
data class JacocoReport (
        @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
        @set:JsonProperty("sessioninfo") var session: JacocoSession? = null,
        @set:JsonProperty("package") var packages: List<JacocoPackage>? = null,
        @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val packageMap by lazy {
        packages.associateNotNullBy { it.name }
    }

    val classesMap by lazy {
        packageMap.values
            .map { it.classMap }
            .fold(emptyMap<String, JacocoClass>()) { a, b -> a + b }
    }
}

@JsonRootName("sessioninfo")
data class JacocoSession(
    @set:JacksonXmlProperty(localName = "id", isAttribute = true) var id: String? = null,
    @set:JacksonXmlProperty(localName = "start", isAttribute = true) var start: Long = 0,
    @set:JacksonXmlProperty(localName = "dump", isAttribute = true) var dump: Long = 0)

data class JacocoCounterResult(val type: JacocoCounterType, val missed: Long, val covered: Long)
@JsonRootName("package")
data class JacocoPackage(
        @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
        @set:JsonProperty("class") var classes: List<JacocoClass>? = null,
        @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val classMap: Map<String, JacocoClass> by lazy {
        classes.associateNotNullBy { it.name }
    }
}

@JsonRootName("class")
data class JacocoClass(
        @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
        @set:JacksonXmlProperty(localName = "sourcefilename", isAttribute = true) var sourceFileName: String? = null,
        @set:JsonProperty("method") var methods: List<JacocoMethod>? = null,
        @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val methodMap by lazy {
        methods.associateNotNullBy { it.name }
    }
}

@JsonRootName("method")
data class JacocoMethod(
        @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
        @set:JacksonXmlProperty(localName = "desc", isAttribute = true) var desc: String? = null,
        @set:JacksonXmlProperty(localName = "line", isAttribute = true) var line: Int = 0,
        @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }
}

@JsonRootName("sourcefile")
data class JacocoSourceFile(
    @set:JsonProperty("line") var lines: List<JacocoLine>? = null,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val lineMap by lazy {
        lines.associateNotNullBy { it.nr }
    }
}

enum class JacocoCounterType {
    INSTRUCTION, BRANCH, LINE, COMPLEXITY, METHOD, CLASS
}

@JsonRootName("counter")
data class JacocoCounter(
        @set:JacksonXmlProperty(localName = "type", isAttribute = true) var type: JacocoCounterType? = null,
        @set:JacksonXmlProperty(localName = "missed", isAttribute = true) var missed: Int = 0,
        @set:JacksonXmlProperty(localName = "covered", isAttribute = true) var covered: Int = 0)

@JsonRootName("line")
data class JacocoLine(
        @set:JacksonXmlProperty(localName = "nr", isAttribute = true) var nr: Int = 0,
        @set:JacksonXmlProperty(localName = "mi", isAttribute = true) var mi: Int = 0,
        @set:JacksonXmlProperty(localName = "ci", isAttribute = true) var ci: Int = 0,
        @set:JacksonXmlProperty(localName = "mb", isAttribute = true) var mb: Int = 0,
        @set:JacksonXmlProperty(localName = "cb", isAttribute = true) var cb: Int = 0)

/**
 * Given a string -- the rest of reading a Jacoco XML results file --
 * returns a [JacocoReport] data class, suitable for subsequent queries.
 */
fun jacocoParser(fileData: String): JacocoReport = kotlinXmlMapper.readValue(fileData)

fun GCoverageMethod.toJacocoCounterType() = when(this) {
    GCoverageMethod.INSTRUCTIONS -> JacocoCounterType.INSTRUCTION
    GCoverageMethod.LINES -> JacocoCounterType.LINE
}

private const val TAG = "JacocoScanner"
private val classSplitRegex = Regex("\$")
private fun matchingClassSpecs(desiredClasses: List<String>, report: JacocoReport): List<String> {
    // In our GGradeProject structure, the names of classes are going to be in normal human
    // form (e.g., edu.rice.autograder.test.Project3) while they'll be in slashy form in
    // the JacocoReport.

    val splitNamesToJacocoNames = report.classesMap.keys
        .associate { it.replace('/', '.' ).split(classSplitRegex) to it }
    val allSplitJacocoNames = splitNamesToJacocoNames.keys
    val allDesiredNames = desiredClasses.map { it.split(classSplitRegex) }

    // TODO: optimize this O(n^2) comparison, after we're sure that it actually works

    val mapping: List<List<String>> = allDesiredNames.flatMap { desired ->
        val matches = allSplitJacocoNames.filter { sjn ->
            desired.zip(sjn).all { (a, b) -> a == b }
        }
        matches
    }

    // We return from the split-list-string names back to the original slashy names
    val result = mapping.map {
        splitNamesToJacocoNames[it] ?: Log.ethrow(TAG, "internal failure, can't find $it")
    }

    return result
}

fun jacocoEvaluator(report: JacocoReport, project: GGradeProject): EvaluatorResult {
    if (project.coveragePoints == 0.0) return passingEvaluatorResult("No test coverage requirement")

    val counterType = project.coverageMethod.toJacocoCounterType()

    val matchingClassNames = matchingClassSpecs(project.coverageClasses, report)
    val matchingClasses = matchingClassNames.map {
        report.classesMap[it] ?: Log.ethrow(TAG, "internal failure: can't find $it")
    }

    val coverageReport = matchingClasses.flatMap {
        val covered = it.counterMap[counterType]?.covered ?: Log.ethrow(TAG, "no coverage number found for $it")
        val missed = it.counterMap[counterType]?.missed ?: Log.ethrow(TAG, "no missed number found for $it")
        val name = it.name ?: Log.ethrow(TAG, "no name found for $it")

        if (covered + missed == 0) emptyList()
        else {
            val percentage = 100.0 * covered / (covered + missed).toDouble()
            listOf("Coverage of %s: %.1f%%".format(name.replace('/', '.'), percentage) to percentage)
        }
    }

    val fails = coverageReport.filter { it.second < project.coveragePercentage }.map { it.first }
    val passing = fails.isEmpty()

    return if (passing) {
        passingEvaluatorResult("Test coverage meets %.0f%% requirement".format(project.coveragePercentage))
    } else {
        EvaluatorResult(false, listOf("TEST COVERAGE" to project.coveragePoints) + fails.map { it to 0.0 })
    }
}
