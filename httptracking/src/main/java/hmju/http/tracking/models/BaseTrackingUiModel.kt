package hmju.http.tracking.models

import androidx.annotation.LayoutRes

/**
 * Description : HTTP Tracking UiModel
 *
 * Created by juhongmin on 2022/04/03
 */
internal abstract class BaseTrackingUiModel(@LayoutRes val layoutId: Int) {
    abstract fun getClassName(): String
    abstract fun areItemsTheSame(diffItem: Any): Boolean
    abstract fun areContentsTheSame(diffItem: Any): Boolean
}
