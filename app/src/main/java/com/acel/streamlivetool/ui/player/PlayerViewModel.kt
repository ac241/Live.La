package com.acel.streamlivetool.ui.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    val anchor = MutableLiveData<Anchor>()
    val streamLink = MutableLiveData<String>()

    internal fun preparePlay(anchor: Anchor?) {
        this.anchor.postValue(anchor)
        anchor?.let { a ->
            viewModelScope.launch(Dispatchers.IO) {
                PlatformDispatcher.getPlatformImpl(a)
                    ?.getStreamingLiveUrl(a)?.let {
                        streamLink.postValue(it)
                    }
            }
        }
    }
}