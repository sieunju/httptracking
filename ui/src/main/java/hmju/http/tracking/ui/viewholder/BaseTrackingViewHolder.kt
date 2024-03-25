package hmju.http.tracking.ui.viewholder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import hmju.http.tracking.R
import hmju.http.tracking.models.*

internal abstract class BaseTrackingViewHolder(
    parent: ViewGroup,
    @LayoutRes layoutId: Int
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
) {
    private val clipboard =
        itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

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
}
