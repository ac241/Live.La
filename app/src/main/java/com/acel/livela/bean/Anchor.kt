package com.acel.livela.bean

import org.jetbrains.annotations.Nullable

class Anchor(
    var platform: String,
    var showId: String,
    var nickname: String,
    var roomId: String, @Nullable var params: String = ""
)
