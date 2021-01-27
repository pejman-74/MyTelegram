package com.mytelegram.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.TriangleEdgeTreatment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.mytelegram.R
import com.mytelegram.data.model.ConversationUser
import com.mytelegram.data.model.MainUser
import com.mytelegram.data.model.RoomUser
import com.mytelegram.data.model.User
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.ui.auth.AcceptCodeFragment
import com.mytelegram.ui.auth.LoginFragment
import com.mytelegram.ui.home.HomeActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


lateinit var mainUser: MainUser

object Utils {
    init {
        System.loadLibrary("native-lib")
    }

    //This key used for before user logged
    external fun apiKey(): String

    //This encryption key for encrypt app communications
    external fun encKey(): String
}

fun User.toRoomUser(roomId: String, role: String) = run {
    RoomUser(userId + roomId, userId, userName, profileUrl, lastSeen, roomId, role)
}

fun User.toConservationUser() = run { ConversationUser(userId, userName, profileUrl, lastSeen) }

fun String.toLocalTime(): String {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        .parse(this.replace("Z", "+00:00"))
    date?.let {
        val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getDefault()
        return dateFormatter.format(date)
    }
    return this
}

fun getCurrentUTCDateTime() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date()) + "Z"

fun MaterialTextView.startWaitForConnectionAnimation() {
    val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
    alphaAnimation.duration = 600
    alphaAnimation.repeatCount = -1
    alphaAnimation.repeatMode = Animation.REVERSE
    startAnimation(alphaAnimation)
}


fun <T> stringToJsonObject(jsonText: String, obj: Class<T>): T =
    Gson().fromJson(jsonText, obj)

fun Context.getPictureDir(): File? {
    return getExternalFilesDir(Environment.DIRECTORY_PICTURES)
}

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

fun getAvatarText(name: String): String {
    val words = name.trim().split(" ", "\u200C")
    return if (words.size > 1)
        "${words[0].first()}\u200c${words[1].first()}"
    else
        try {
            words[0].first().toString()
        } catch (e: Exception) {
            "*"
        }
}


fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}


fun View.snackBar(message: String) {
    hideKeyboard()
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    snackBar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSnackBar))
    snackBar.setLocation(Gravity.CENTER)
    snackBar.show()
}

fun Snackbar.setLocation(gravity: Int) {
    val view = this.view
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.gravity = gravity
    view.layoutParams = params
}

fun Snackbar.setBackgroundColor(color: Int) {
    view.setBackgroundColor(color)
}

fun Fragment.fatalAlertDialog(message: String, action: (() -> Unit)? = null) {
    if (this is LoginFragment || this is AcceptCodeFragment)
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.app_name)
            .setMessage(message)
            .setNeutralButton(requireContext().getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }.apply {
                action?.let {
                    setPositiveButton(context.getText(R.string.retry)) { dialog, _ ->
                        action.invoke()
                        dialog.dismiss()
                    }
                }
            }.setCancelable(false)
            .show()
    else
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.app_name)
            .setMessage(message)
            .setNegativeButton(requireContext().getString(R.string.logout)) { dialog, _ ->
                (this.requireActivity() as HomeActivity).logOut()
                dialog.dismiss()
            }.setCancelable(false)
            .show()
}

fun Context.showLongToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
fun MaterialCardView.setTailLength(location: String, length: Float) {
    val edgeTreatment = TriangleEdgeTreatment(length, false)
    doOnLayout {
        when (location) {
            "left" -> {
                val offsetEdgeTreatment =
                    OffsetEdgeTreatment(edgeTreatment, (height / 2).toFloat())
                shapeAppearanceModel =
                    shapeAppearanceModel.toBuilder().setLeftEdge(offsetEdgeTreatment)
                        .build()
            }
            "right" -> {
                val offsetEdgeTreatment =
                    OffsetEdgeTreatment(edgeTreatment, (height / 2).inv().toFloat())
                shapeAppearanceModel =
                    shapeAppearanceModel.toBuilder().setRightEdge(offsetEdgeTreatment).build()

            }
        }
    }
}

fun Fragment.handelApiError(failure: Resource.Failure) {

    when (val exception = failure.exception) {
        is ApiException.ErrorException -> {
            exception.message?.let { requireView().snackBar(it) }
        }
        is ApiException.FailedException -> {
            exception.message?.let { println(it) }
        }
        is ApiException.ConvertException -> {
            exception.message?.let { println(it) }
        }
        is ApiException.UnknownException -> requireView().snackBar(
            if (exception.message.isNullOrEmpty()) getString(R.string.unknown_error)
            else
                exception.message!!
        )

    }
}