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

// We also have to deal with the most complicated policies. We have GradeCoverage annotations
// on packages and classes, and they've got an "exclude" flag as well. This means that we'll
// have some fairly complex logic to resolve for a given {package, class} whether we're supposed
// to pay attention to its coverage numbers. We're going to need to do this recursively, so
// we may have a "positive" annotation on edu.rice.foo and a "negative" annotation.edu.rice.foo.bar
// which means that edu.rice.foo.baz should be included.

@JsonRootName("report")
internal data class JacocoReport(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
    @set:JsonProperty("sessioninfo") var session: JacocoSession? = null,
    @set:JsonProperty("package") var packages: List<JacocoPackage>? = null,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null
) {

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
internal data class JacocoSession(
    @set:JacksonXmlProperty(localName = "id", isAttribute = true) var id: String? = null,
    @set:JacksonXmlProperty(localName = "start", isAttribute = true) var start: Long = 0,
    @set:JacksonXmlProperty(localName = "dump", isAttribute = true) var dump: Long = 0
)

internal data class JacocoCounterResult(val type: JacocoCounterType, val missed: Long, val covered: Long)
@JsonRootName("package")
internal data class JacocoPackage(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
    @set:JsonProperty("class") var classes: List<JacocoClass>? = null,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    // Note: we're standardizing class names in classMap to use dots rather than slashes,
    // making them consistent with everything else in AnnoAutoGrader.
    val classMap: Map<String, JacocoClass> by lazy {
        classes.associateNotNullBy { it.name?.replace('/', '.') }
    }
}

@JsonRootName("class")
internal data class JacocoClass(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
    @set:JacksonXmlProperty(localName = "sourcefilename", isAttribute = true) var sourceFileName: String? = null,
    @set:JsonProperty("method") var methods: List<JacocoMethod>? = null,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val methodMap by lazy {
        methods.associateNotNullBy { it.name }
    }
}

@JsonRootName("method")
internal data class JacocoMethod(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
    @set:JacksonXmlProperty(localName = "desc", isAttribute = true) var desc: String? = null,
    @set:JacksonXmlProperty(localName = "line", isAttribute = true) var line: Int = 0,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }
}

@JsonRootName("sourcefile")
internal data class JacocoSourceFile(
    @set:JsonProperty("line") var lines: List<JacocoLine>? = null,
    @set:JsonProperty("counter") var counters: List<JacocoCounter>? = null
) {

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
internal data class JacocoCounter(
    @set:JacksonXmlProperty(localName = "type", isAttribute = true) var type: JacocoCounterType? = null,
    @set:JacksonXmlProperty(localName = "missed", isAttribute = true) var missed: Int = 0,
    @set:JacksonXmlProperty(localName = "covered", isAttribute = true) var covered: Int = 0
)

@JsonRootName("line")
internal data class JacocoLine(
    @set:JacksonXmlProperty(localName = "nr", isAttribute = true) var nr: Int = 0,
    @set:JacksonXmlProperty(localName = "mi", isAttribute = true) var mi: Int = 0,
    @set:JacksonXmlProperty(localName = "ci", isAttribute = true) var ci: Int = 0,
    @set:JacksonXmlProperty(localName = "mb", isAttribute = true) var mb: Int = 0,
    @set:JacksonXmlProperty(localName = "cb", isAttribute = true) var cb: Int = 0
)

/**
 * Given a string -- the rest of reading a Jacoco XML results file --
 * returns a [JacocoReport] data class, suitable for subsequent queries.
 */
internal fun jacocoParser(fileData: String): JacocoReport = kotlinXmlMapper.readValue(fileData)

internal fun GCoverageStyle.toJacocoCounterType() = when (this) {
    GCoverageStyle.INSTRUCTIONS -> JacocoCounterType.INSTRUCTION
    GCoverageStyle.LINES -> JacocoCounterType.LINE
}

private const val TAG = "JacocoScanner"

private fun matchingClassSpecs(coverages: List<GGradeCoverage>, report: JacocoReport): List<String> {
    // In our GGradeProject structure, the names of classes are going to be in normal human
    // form (e.g., edu.rice.autograder.test.Project3) while they'll be in slashy form in
    // the JacocoReport.

    val packageSpecs = coverages.filter { it.scope == GCoverageScope.PACKAGE }.sortedBy { it.name }
    val classSpecs = coverages.filter { it.scope == GCoverageScope.CLASS }.sortedBy { it.name }

    val namesToTestCoverage = report.classesMap.keys.filter { className ->
        // we're working our way down from the most general to the most specific package annotation
        // then the most general to teh most specific class annotation (the sorting above is essential
        // to make this happen). The logic here is that the last relevant annotation wins, so an inner
        // "including" annotation overrides an external "excluding" annotation.
        val relevantSpecs = packageSpecs.filter { className.startsWith(it.name + ".") } +
                classSpecs.filter { className.startsWith(it.name + ".") }
        val result = relevantSpecs.fold(false) { prior, next -> !next.excluded }
        result
    }

    return namesToTestCoverage
}

internal fun jacocoEvaluator(report: JacocoReport, project: GGradeProject): EvaluatorResult {
    if (project.coveragePoints == 0.0) return passingEvaluatorResult("No test coverage requirement")

    val counterType = project.coverageStyle.toJacocoCounterType()

    val matchingClassNames = matchingClassSpecs(project.coverageAnnotations, report)
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
