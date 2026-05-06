package com.adjaba.news

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.URL
import java.util.Locale

class NewsHandler(var index: Int) {

    lateinit var result: MutableList<RssItem>
    suspend fun fetchRss(url: String): InputStream {
        val connection = URL(url).openConnection()
        return connection.getInputStream()
    }

    fun parseRss(stream: InputStream): MutableList<RssItem> {
        val items = mutableListOf<RssItem>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()

        parser.setInput(stream, null)

        var event = parser.eventType

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var thumbnail: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {

                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "item" -> {
                            title = null
                            link = null
                            description = null
                            thumbnail = null
                        }

                        "title" -> if (title == null) title = parser.nextText()
                        "link" -> if (link == null) link = parser.nextText()
                        "description" -> if (description == null) description =
                            parser.nextText()

                        "thumbnail", "media:thumbnail", "media:content" -> {
                            thumbnail = parser.getAttributeValue(null, "url")
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        items.add(
                            RssItem(
                                title ?: "",
                                link ?: "",
                                description ?: "",
                                "", "", thumbnail ?: ""
                            )
                        )
                    }
                }
            }

            event = parser.next()
        }

        return items
    }


    suspend fun getCountryFromCity(context: Context, cityName: String): String? {
        val country = try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(cityName, 1)

            if (!results.isNullOrEmpty()) {
                results[0].countryName ?: "United Kingdom"
            } else {
                Toast.makeText(context, "News unavailable, try again", Toast.LENGTH_SHORT).show()
                null
            }
        } catch (e: Exception) {
            null
        }
        return country
    }

    fun load(
        cityName: String,
        context: Context,
        onLoaded: (List<RssItem>, Int) -> Unit,
        onProgressBar: (Int) -> Unit
    ) {

        GlobalScope.launch(Dispatchers.IO) {
            val country = getCountryFromCity(context, cityName)
            if (Utils.countryRss[country.toString()].toString().length < 5) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "News Unavailable Now",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (Utils.NewsList.isEmpty()) {
                    val stream = fetchRss(Utils.countryRss[country].toString())
                    result = parseRss(stream)
                    Utils.NewsList = result
                }

                withContext(Dispatchers.Main) {
                    onLoaded(Utils.NewsList, 1)
                    onProgressBar(1)


                }
            }
        }
    }

}