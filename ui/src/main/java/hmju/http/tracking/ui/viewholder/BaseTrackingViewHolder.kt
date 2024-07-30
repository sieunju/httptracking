package hmju.http.tracking.ui.viewholder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import hmju.http.tracking.R
import hmju.http.tracking.models.*
import hmju.http.tracking.models.BaseTrackingUiModel

internal abstract class BaseTrackingViewHolder(
    parent: ViewGroup,
    @LayoutRes layoutId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
) {
    private val clipboard = itemView.context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager

    @Throws(Exception::class)
    abstract fun onBindView(model: BaseTrackingUiModel)

    /**
     * 길게 눌렀을때 해당 내용 복사 되도록 처리
     */
    protected fun simpleLongClickCopy(txt: String) {
        val clip: ClipData = ClipData.newPlainText("HttpLogging", txt)
        clipboard.setPrimaryClip(clip)
        itemView.context.vibrate()
        Toast.makeText(itemView.context, R.string.txt_copy_success, Toast.LENGTH_SHORT).show()
        // shareActivity(txt)
    }

    /**
     * 진동 피드백 처리 함수
     */
    private fun Context.vibrate() {
        val duration = 100L
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, 50))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * 복사한 텍스트 공유하기
     */
    private fun shareActivity(txt: String) {
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, txt)
            type = "text/plain"
        }.let {
            val shareIntent = Intent.createChooser(it, null)
            itemView.context.startActivity(shareIntent)
        }
    }

    internal fun AppCompatTextView.changeColor(hexCode: String) {
        val color = try {
            Color.parseColor(hexCode)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return
        }
        setTextColor(color)
    }

    internal fun View.changeBgColor(hexCode: String) {
        val color = try {
            Color.parseColor(hexCode)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return
        }
        val bg = background
        if (bg is ColorDrawable && bg.color != color) {
            bg.color = color
        } else {
            setBackgroundColor(color)
        }
    }

    internal fun View.changeVisible(isVisible: Boolean) {
        if (isVisible) {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
        } else {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        }
    }

    internal fun AppCompatTextView.changeText(str: CharSequence) {
        if (text != str) {
            text = str
        }
    }
}
