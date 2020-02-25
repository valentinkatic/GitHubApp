package com.katic.api.log

import android.content.Context
import com.katic.api.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

/**
 * Central log interface to enable easy change of loggers later if needed (log4j, slf4j ...)
 * and central log enabling/disabing.
 * Currently just a simple wrapper around standard Android [android.util.Log].
 *
 *
 * Usage:<br></br>
 * <pre>
 * private static final Log log = Log.getLog("SomeTag");
 * ...
 * if (Log.DEBUG) log.d("something");
 * if (Log.INFO) log.i("something");
</pre> *
 *
 *
 *
 * Enabling/disabling logs: set [.VERBOSE], [.DEBUG] etc. to desired boolean constants
 * and wrap log statements with them:<br></br>
 * <pre>
 * if (Log.DEBUG) log.d("something");
</pre> *
 * In that way we can remove logs from the code for release builds if needed by defining those
 * constants to false (compiler will detect code inside `if (false)` and remove it).
 *
 *
 *
 * Adding another logger: we can just rewrite this class to delegate
 * log calls to another logger.
 *
 */
class Log(val tag: String, private val mLogger: Logger?) {

    fun v(msg: String?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.FINEST, msg)
        }
        return android.util.Log.v(tag, msg, null)
    }

    fun v(msg: String?, tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.FINEST, msg, tr)
        }
        return android.util.Log.v(tag, msg, tr)
    }

    fun d(msg: String?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.FINE, msg)
        }
        return android.util.Log.d(tag, msg, null)
    }

    fun d(msg: String?, tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.FINE, msg, tr)
        }
        return android.util.Log.d(tag, msg, tr)
    }

    fun i(msg: String?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.INFO, msg)
        }
        return android.util.Log.i(tag, msg, null)
    }

    fun i(msg: String?, tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.INFO, msg, tr)
        }
        return android.util.Log.i(tag, msg, tr)
    }

    fun w(msg: String?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.WARNING, msg)
        }
        return android.util.Log.w(tag, msg, null)
    }

    fun w(msg: String?, tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.WARNING, msg, tr)
        }
        return android.util.Log.w(tag, msg, tr)
    }

    fun w(tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.WARNING, null, tr)
        }
        return android.util.Log.w(tag, tr)
    }

    fun e(msg: String?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.SEVERE, msg)
        }
        return android.util.Log.e(tag, msg)
    }

    fun e(msg: String?, tr: Throwable?): Int {
        if (JAVA_LOG_READY && mLogger != null) {
            mLogger.log(Level.SEVERE, msg, tr)
        }
        return android.util.Log.e(tag, msg, tr)
    }

    companion object {
        private const val TAG = "Log"
        val LOG: Boolean = BuildConfig.LOG
        val VERBOSE = LOG
        val DEBUG = LOG
        val INFO = LOG
        val WARN = LOG
        val ERROR = LOG
        private val JAVA_LOG = LOG
        private var JAVA_LOG_READY = false
        /**
         * @param context context
         * @return directory where logs will be stored
         */
        fun getLogsDir(context: Context): File? {
            return if (JAVA_LOG) context.getExternalFilesDir("logs") else null
        }

        /**
         * Init logger
         *
         * @param context context
         */
        fun init(context: Context) {
            if (LOG && JAVA_LOG) {
                try {
                    val logsDir = getLogsDir(context)
                    if (logsDir == null) {
                        android.util.Log.i(
                            TAG,
                            "init: log not inited - no external storage"
                        )
                        return
                    }
                    val path = logsDir.absolutePath
                    android.util.Log.d(TAG, "init: path: $path")
                    val root = Logger.getLogger("")
                    root.level = Level.FINEST
                    // remove default handlers
                    val handlers = root.handlers
                    for (handler in handlers) {
                        root.removeHandler(handler)
                    }
                    // create file handler with custom formatter
                    val fileHandler =
                        FileHandler("$path/log-%g.txt", 1000000, 10, false)
                    fileHandler.level = Level.FINEST
                    fileHandler.formatter = object : Formatter() {
                        override fun format(r: LogRecord): String {
                            val df: DateFormat = SimpleDateFormat(
                                "dd.MM.yyyy HH:mm:ss.SSS",
                                Locale.getDefault()
                            )
                            val sb = StringBuilder()
                            sb.append(df.format(Date(r.millis))).append(' ')
                            sb.append(r.threadID).append(": ")
                            sb.append(r.loggerName).append(' ')
                            sb.append(r.sourceMethodName).append(": ")
                            //                        sb.append(r.getLevel().getName()).append(": ");
                            sb.append(formatMessage(r)).append('\n')
                            if (r.thrown != null) {
                                sb.append("Throwable occurred: ")
                                val t = r.thrown
                                val sw = StringWriter()
                                val pw = PrintWriter(sw)
                                t.printStackTrace(pw)
                                sb.append(sw.toString())
                            }
                            return sb.toString()
                        }
                    }
                    root.addHandler(fileHandler)
                    android.util.Log.i(TAG, "init: log inited")
                    JAVA_LOG_READY = true
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "init", e)
                }
            }
        }

        /**
         * Flushes log buffers if any
         */
        fun flush() {
            if (JAVA_LOG_READY) {
                val handlers =
                    Logger.getLogger("").handlers
                if (handlers != null) {
                    for (handler in handlers) {
                        handler.flush()
                    }
                }
            }
        }

        fun getLog(tag: String): Log {
            return Log(tag, Logger.getLogger(tag))
        }

        fun getLog(clazz: Class<*>): Log {
            return getLog(clazz.simpleName)
        }
    }

}