/*
 * AnnoAutoGrader
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
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
val kotlinXmlMapper: ObjectMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerKotlinModule()
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

@JsonRootName("checkstyle")
data class CheckStyleResults(
    @set:JsonProperty("file")
    var files: List<CheckStyleFile> = emptyList())

@JsonRootName("file")
data class CheckStyleFile(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var name: String? = null,

    @set:JsonProperty("error")
    var errors: List<CheckStyleError> = emptyList())

@JacksonXmlRootElement(localName = "error")
data class CheckStyleError(
    @set:JacksonXmlProperty(localName = "line", isAttribute = true)
    var line: String? = null,

    @set:JacksonXmlProperty(localName = "severity", isAttribute = true)
    var severity: String? = null,

    @set:JacksonXmlProperty(localName = "message", isAttribute = true)
    var message: String? = null,

    @set:JacksonXmlProperty(localName = "source", isAttribute = true)
    var source: String? = null
)

private const val TAG = "CheckStyleScanner"

fun checkStyleMissing(moduleName: String) =
    "CheckStyle ($moduleName): report not found" to false

fun checkStyleParser(fileData: String): CheckStyleResults? {
    Log.i(TAG, "checkStyleParser: ${fileData.length} bytes")

    return if (fileData.isEmpty()) null
    else kotlinXmlMapper.readValue(fileData)
}

/** You'll typically run this twice: once for "test" and once for "main". */
fun CheckStyleResults?.eval(moduleName: String): Pair<String, Boolean> {
    if (this == null) {
        return checkStyleMissing(moduleName)
    }

    val numFiles = files.count()
    val numCleanFiles = files.filter { it.errors.isEmpty() }.count()

    val errorMsg = "CheckStyle ($moduleName): $numCleanFiles of $numFiles files passed"
    Log.i(TAG, "checkStyleEvaluator: $moduleName --> $errorMsg")

    val passing = numFiles == numCleanFiles
    return errorMsg to passing
}
