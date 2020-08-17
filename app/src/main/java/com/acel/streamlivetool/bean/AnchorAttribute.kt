package com.acel.streamlivetool.bean

class AnchorAttribute(
    val anchor: Anchor,
    val status: Boolean,
    val title: String,
    val avatar: String? = null,
    val keyFrame: String? = null,
    val secondaryStatus: String? = null,
    val typeName: String? = null
)