//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// CheckStyle's XML looks like this:
// <?xml version="1.0" encoding="UTF-8"?>
// <checkstyle version="8.17">
// <file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/ListTheories.java">
// </file>
// <file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/QtHelpers.java">
// <error line="33" severity="warning" message="Line is longer than 140 characters (found 224)." source="com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck"/>
// </file>
// <file name="/Users/dwallach/IdeaProjects/comp215-code/src/test/java/edu/rice/qt/SequenceGenerators.java">
// </file>
// ...
// </checkstyle>

// So, what we're looking for are any files that have <error> entities inside. If we see
// any of them, then we indicate a failure.

// Almost useful tutorial web sites:
// https://dzone.com/articles/parse-xml-to-java-objects-using-jackson
// https://medium.com/@foxjstephen/how-to-actually-parse-xml-in-java-kotlin-221a9309e6e8

/** General-purpose Jackson XML mapper, used everywhere. */
internal val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerKotlinModule()
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

@JsonRootName("checkstyle")
internal data class CheckStyleResults(
    @set:JsonProperty("file") var files: List<CheckStyleFile> = emptyList())

@JsonRootName("file")
internal data class CheckStyleFile(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String? = null,
    @set:JsonProperty("error") var errors: List<CheckStyleError> = emptyList())

@JacksonXmlRootElement(localName = "error")
internal data class CheckStyleError(
    @set:JacksonXmlProperty(localName = "line", isAttribute = true) var line: String? = null,
    @set:JacksonXmlProperty(localName = "severity", isAttribute = true) var severity: String? = null,
    @set:JacksonXmlProperty(localName = "message", isAttribute = true) var message: String? = null,
    @set:JacksonXmlProperty(localName = "source", isAttribute = true) var source: String? = null
)

internal fun checkStyleParser(fileData: String): CheckStyleResults = kotlinXmlMapper.readValue(fileData)

/** You'll typically run this twice: once for "test" and once for "main". */
internal fun checkStyleEvaluator(moduleName: String, results: CheckStyleResults, deduction: Double = 1.0): EvaluatorResult {
    val numFiles = results.files.count()
    val numCleanFiles = results.files.filter { it.errors.isEmpty() }.count()
    val errorMsg = "CheckStyle ($moduleName): $numCleanFiles of $numFiles files passed"
    val passing = numFiles == numCleanFiles
    return EvaluatorResult(passing, listOf(errorMsg to if (passing) 0.0 else deduction))
}
