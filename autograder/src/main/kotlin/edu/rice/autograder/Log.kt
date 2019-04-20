/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is a simplified version of the Android logging system
 * ([http://developer.android.com/reference/android/util/Log.html](http://developer.android.com/reference/android/util/Log.html))
 * that uses slf4j / [Logback](https://logback.qos.ch/) as its backend.
 */
object Log {
    // We need to maintain one "logger" per "tag". We keep all of that inside this loggerMap.
    private val loggerMap = HashMap<String, Logger>()

    const val ALL = 1
    const val ERROR = 0
    const val NOTHING = -1

    private const val TAG = "Log"
    var logLevel = NOTHING
        set(level) {
            field = when (level) {
                ALL, ERROR, NOTHING -> level
                else -> throw IllegalArgumentException("Unknown log level: $level")
            }
        }

    fun setLogLevel(level: String) {
        logLevel = when (level) {
            "all", "ALL" -> ALL
            "error", "ERROR" -> ERROR
            "nothing", "NOTHING" -> NOTHING
            else -> throw IllegalArgumentException("Supported log levels: all, error, nothing")
        }
    }

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
        // Compute once per tag, then memoize.
        loggerMap.getOrElse(tag) { LoggerFactory.getLogger(tag) }

    /**
     * Many of the logging functions let you delay the computation of the log string, such that if
     * logging is turned off, then that computation will never need to happen. That means hiding the
     * computation inside a lambda. So far so good.
     *
     * Normally, we'd just call [msgFunc] to fetch the string behind the lambda, but what if
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
    fun e(tag: String, msg: Any?) {
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
     * in [String.format] or [java.io.PrintStream.printf] for constructing the message to be logged.
     * The error message is logged **and** also included in a [RuntimeException] which is thrown.
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
