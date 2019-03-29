//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

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

// So, the factory tests have funny names with an index number afterward, but otherwise look just like regular tests.
