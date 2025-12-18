package com.adjaba.news

data class RssItem(
    var title: String? = null,
    var link: String? = null,
    var description: String? = null,
    var pubDate: String? = null,
    var guid: String? = null,
    var thumbnailUrl: String? = null,
    var thumbnailWidth: Int? = null,
    var thumbnailHeight: Int? = null
)
