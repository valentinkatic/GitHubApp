package com.katic.githubapp.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.katic.api.log.Log
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import retrofit2.HttpException
import java.util.concurrent.Callable


class UiUtils {

    companion object {
        private val log = Log.getLog("UiUtils")

        fun handleUiError(
            activity: Activity?,
            throwable: Throwable?
        ) {
            var message: String? = "An error occurred, please try again."

            // check if this is service or network error
            if (throwable is HttpException) {
                message = try {
                    val obj = JSONObject(throwable.response()?.errorBody()?.string() ?: "")
                    obj.getString("message")
                } catch (e: Exception) {
                    if (Log.LOG) log.e("handleUiError parsing exception", e)
                    "Service error. Please, try again."
                }
            }

            if (Log.LOG) log.d("displayUiError: dialog")

            val dialog = AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)

            try {
                dialog.show()
            } catch (e: Exception) {
                if (Log.LOG) log.e("handleUiError", e)
            }

        }

        fun getSpannableString(
            prefix: String,
            highlighted: String,
            onClickListener: Callable<Unit>
        ): SpannableString {
            val iStart = prefix.length
            val iEnd = iStart + (highlighted.length)
            val ssText = SpannableString("$prefix$highlighted")
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClickListener.call()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = Color.BLUE
                }
            }
            ssText.setSpan(clickableSpan, iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return ssText
        }

        fun openUrl(context: Context, url: String) {
            if (Log.LOG) log.d("openUrl: $url")
            var url = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }
}

/**
 * Runs [run] block and calls [catch] block if [run] throws exception
 * or calls [cancel] block if coroutine is canceled
 * (if [CancellationException]) is thrown.
 */
suspend inline fun runCatchCancel(
    run: () -> Unit,
    catch: (t: Throwable) -> Unit,
    cancel: (() -> Unit)
) {
    try {
        run()
    } catch (t: Throwable) {
        if (t !is CancellationException) {
            catch(t)
        } else {
            cancel()
        }
    }
}
