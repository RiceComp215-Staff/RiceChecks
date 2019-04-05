//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is a simplified version of the Android logging system ([http://developer.android.com/reference/android/util/Log.html](http://developer.android.com/reference/android/util/Log.html))
 * that uses slf4j / [Logback](https://logback.qos.ch/) as its backend.
 */
object Log {
    // We need to maintain one "logger" per "tag". We keep all of that inside this loggerMap.
    private val loggerMap = HashMap<String, Logger>()

    const val ALL = 1
    const val ERROR = 0
    const val NOTHING = -1

    private const val TAG = "Log"
    private var logLevel = NOTHING

    fun logProperties() =
        listOf("java.version",
                "java.vm.version",
                "java.runtime.name",
                "java.home",
                "java.vendor",
                "java.vm.name",
                "user.dir")
                .forEach {
                    iformat(TAG, "System property: %-17s -> %s", it, System.getProperty(it))
                }

    private fun logger(tag: String): Logger =
        // Once we have a Logback logger for a tag, we don't want to make a new one, so we save
        // the old one. Java's HashMap supports exactly this sort of functionality via it's
        // computeIfAbsent method. In other words, we're *memoizing*, which we'll talk about more
        // later in the semester.
        loggerMap.getOrElse(tag) { LoggerFactory.getLogger(tag) }

    /**
     * Set the log level.
     *
     * @param level (one of Log.ALL, Log.ERROR, or Log.NOTHING)
     */
    fun setLogLevel(level: Int) {
        if (level == ALL || level == ERROR || level == NOTHING) {
            logLevel = level
        } else {
            throw IllegalArgumentException("Unknown log level: $level")
        }
    }

    /**
     * Set the log level.
     *
     * @param level (one of "all", "error", "nothing")
     */
    fun setLogLevel(level: String) = when (level) {
        "all", "ALL" -> logLevel = ALL
        "error", "ERROR" -> logLevel = ERROR
        "nothing", "NOTHING" -> logLevel = NOTHING
        else -> throw IllegalArgumentException("Supported log levels: all, error, nothing")
    }

    /**
     * Many of the logging functions let you delay the computation of the log string, such that if
     * logging is turned off, then that computation will never need to happen. That means hiding the
     * computation inside a lambda. So far so good.
     *
     *
     * Normally, we'd just call msgFunc.get() to fetch the string behind the lambda, but what if
     * there's an exception generated in the process of returning that string? We don't want the Log
     * library to ever throw an exception. Solution? We quietly eat exceptions here and, when they do
     * occur, the ultimate log string will reflect that failure, but THE SHOW MUST GO ON!
     */
    private fun safeGet(msgFunc: () -> Any?): String =
        try {
            msgFunc().toString()
        } catch (throwable: Throwable) {
            String.format("Log string supplier failure!: %s", throwable)
        }

    /**
     * Information logging. Lambda variant allows the string to be evaluated only if needed.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msgFunc Lambda providing the string or object to be logged
     * @param th Throwable, exception, error, etc. to be included in the log
     */
    fun i(tag: String, msgFunc: () -> Any?, th: Throwable) {
        // Engineering / performance note:
        //
        // This logging function and every other logging function tries to
        // bail out as early as possible, to avoid any unnecessary
        // computation if the logging level is disabled.
        //
        // There are actually two opportunities for us to detect when a
        // log event will never happen.  First, we can check the logLevel,
        // which is internal to edu.rice.util.Log. After that, Logback has
        // its own checking that it will do. We make both checks
        // explicitly here before calling safeGet() to extract the string
        // we're about to log.
        //
        // Elsewhere in Comp215, you shouldn't go to the level of trouble
        // that we do in edu.rice.util.Log, especially since it appears to
        // violate our "don't repeat yourself" principle, but since it's
        // our goal to make these functions outrageously cheap when
        // logging is disabled, we need to go through some extra hoops.

        if (logLevel == ALL) {
            val l = logger(tag)
            if (l.isInfoEnabled) {
                l.info(safeGet(msgFunc), th)
            }
        }
    }

    /**
     * Information logging. Lambda variant allows the string to be evaluated only if needed.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msgFunc Lambda providing the string or object to be logged
     */
    fun i(tag: String, msgFunc: () -> Any?) {
        if (logLevel == ALL) {
            val l = logger(tag)
            if (l.isInfoEnabled) {
                l.info(safeGet(msgFunc))
            }
        }
    }

    /**
     * Information logging. Logs the message.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg String or object to be logged
     */
    fun i(tag: String, msg: Any?) {
        if (logLevel == ALL) {
            val l = logger(tag)
            if (l.isInfoEnabled) {
                l.info(msg.toString())
            }
        }
    }

    /**
     * Information logging with string formatting. Uses the same [java.util.Formatter] syntax as
     * used in [String.format] or [java.io.PrintStream.printf] for constructing the message to be logged.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg Formatting string to be logged
     * @param args Optional formatting arguments
     */
    fun iformat(tag: String, msg: String, vararg args: Any?) {
        if (logLevel == ALL) {
            val l = logger(tag)
            if (l.isInfoEnabled) {
                l.info(msg.format(*args))
            }
        }
    }

    /**
     * Error logging. Lambda variant allows the string to be evaluated only if needed.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msgFunc Lambda providing the string or object to be logged
     */
    fun e(tag: String, msgFunc: () -> Any?) {
        if (logLevel >= ERROR) {
            val l = logger(tag)
            if (l.isErrorEnabled) {
                l.error(safeGet(msgFunc))
            }
        }
    }

    /**
     * Error logging. Logs the message.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg String or object to be logged
     */
    fun e(tag: String, msg: Any) {
        if (logLevel >= ERROR) {
            val l = logger(tag)
            if (l.isErrorEnabled) {
                l.error(msg.toString())
            }
        }
    }

    /**
     * Error logging. Lambda variant allows the string to be evaluated only if needed.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msgFunc Lambda providing the string or object to be logged
     * @param th Throwable, exception, error, etc. to be included in the log
     */
    fun e(tag: String, msgFunc: () -> Any?, th: Throwable) {
        if (logLevel >= ERROR) {
            val l = logger(tag)
            if (l.isErrorEnabled) {
                l.error(safeGet(msgFunc), th)
            }
        }
    }

    /**
     * Error logging. Logs the message.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg String or object to be logged
     * @param th Throwable, exception, error, etc. to be included in the log
     */
    fun e(tag: String, msg: Any?, th: Throwable) {
        if (logLevel >= ERROR) {
            val l = logger(tag)
            if (l.isErrorEnabled) {
                l.error(msg.toString(), th)
            }
        }
    }

    /**
     * Error logging with string formatting. Uses the same [java.util.Formatter] syntax as used
     * in [String.format] or [java.io.PrintStream.printf] for constructing the message to be logged.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg Formatting string to be logged
     * @param args Optional formatting arguments
     */
    fun eformat(tag: String, msg: String, vararg args: Any?) {
        if (logLevel >= ERROR) {
            val l = logger(tag)
            if (l.isErrorEnabled) {
                l.error(msg.format(*args))
            }
        }
    }

    /**
     * Error logging with string formatting. Uses the same [java.util.Formatter] syntax as used
     * in [String.format] or [java.io.PrintStream.printf] for constructing the message to be logged. The error message is logged **and**
     * also included in a [RuntimeException] which is thrown.
     *
     * @param tag String indicating which code is responsible for the log message
     * @param msg Formatting string to be logged
     * @param args Optional formatting arguments
     * @throws RuntimeException with the given message
     */
    fun ethrow(tag: String, msg: String, vararg args: Any?): Nothing {
        val s = msg.format(*args)
        e(tag, s)
        throw RuntimeException(s)
    }
}
