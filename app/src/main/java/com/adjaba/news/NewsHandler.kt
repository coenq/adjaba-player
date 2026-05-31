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

    // Strip HTML tags and decode common HTML entities from text
    private fun stripHtmlAndDecode(html: String?): String {
        if (html.isNullOrEmpty()) return ""
        var text = html.replace(Regex("<[^>]*>"), "")
        text = text.replace("&nbsp;", " ")
        text = text.replace("&lt;", "<")
        text = text.replace("&gt;", ">")
        text = text.replace("&amp;", "&")
        text = text.replace("&quot;", "\"")
        text = text.replace("&#39;", "'")
        text = text.replace("&apos;", "'")
        text = text.replace(Regex("\\s+"), " ").trim()
        return text
    }

    suspend fun fetchRss(url: String): InputStream {
        return URL(url).openConnection().getInputStream()
    }

    fun parseRss(stream: InputStream, sourceName: String = ""): MutableList<RssItem> {
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
                XmlPullParser.START_TAG -> when (parser.name) {
                    "item" -> {
                        title = null; link = null; description = null; thumbnail = null
                    }
                    "title" -> if (title == null) title = stripHtmlAndDecode(parser.nextText())
                    "link" -> if (link == null) link = parser.nextText()
                    "description" -> if (description == null) description = stripHtmlAndDecode(parser.nextText())
                    "thumbnail", "content" -> {
                        // <media:thumbnail> / <media:content url="...">
                        val u = parser.getAttributeValue(null, "url")
                        if (!u.isNullOrEmpty()) thumbnail = u
                    }
                    "enclosure" -> {
                        val type = parser.getAttributeValue(null, "type") ?: ""
                        if (type.startsWith("image")) {
                            val u = parser.getAttributeValue(null, "url")
                            if (!u.isNullOrEmpty() && thumbnail.isNullOrEmpty()) thumbnail = u
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name == "item") {
                    // Fallback: extract first <img src> from description HTML
                    if (thumbnail.isNullOrEmpty() && !description.isNullOrEmpty()) {
                        val m = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(description!!)
                        if (m != null) thumbnail = m.groupValues[1]
                    }
                    items.add(RssItem(title ?: "", link ?: "", description ?: "", "", "", thumbnail ?: "", source = sourceName))
                }
            }
            event = parser.next()
        }
        return items
    }

    // Derive a friendly source name from the RSS URL
    private fun sourceNameFromUrl(url: String): String = when {
        url.contains("bbc.co.uk")         -> "BBC News"
        url.contains("cnn.com")           -> "CNN"
        url.contains("reuters.com")       -> "Reuters"
        url.contains("aljazeera.com")     -> "Al Jazeera"
        url.contains("foxnews.com")       -> "Fox News"
        url.contains("nbcnews.com")       -> "NBC News"
        url.contains("abcnews.go.com")    -> "ABC News"
        url.contains("npr.org")           -> "NPR"
        url.contains("theguardian.com")   -> "The Guardian"
        url.contains("nytimes.com")       -> "New York Times"
        url.contains("washingtonpost.com")-> "Washington Post"
        url.contains("apnews.com")        -> "AP News"
        url.contains("thehindu.com")      -> "The Hindu"
        url.contains("ndtv.com")          -> "NDTV"
        url.contains("timesofindia.com")  -> "Times of India"
        url.contains("dawn.com")          -> "Dawn"
        url.contains("arabnews.com")      -> "Arab News"
        url.contains("gulfnews.com")      -> "Gulf News"
        url.contains("khaleejtimes.com")  -> "Khaleej Times"
        url.contains("lemonde.fr")        -> "Le Monde"
        url.contains("spiegel.de")        -> "Der Spiegel"
        url.contains("smh.com.au")        -> "Sydney Morning Herald"
        url.contains("abc.net.au")        -> "ABC Australia"
        url.contains("globalnews.ca")     -> "Global News"
        url.contains("cbc.ca")            -> "CBC"
        url.contains("japantimes.co.jp")  -> "Japan Times"
        url.contains("scmp.com")          -> "South China Morning Post"
        url.contains("straitstimes.com")  -> "The Straits Times"
        url.contains("nationalnews.com")  -> "National News"
        else                              -> "News"
    }

    private suspend fun getCountryFromCity(context: Context, cityName: String): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(cityName, 1)
            if (!results.isNullOrEmpty()) results[0].countryName ?: "" else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun load(
        cityName: String,
        context: Context,
        onLoaded: (List<RssItem>, Int) -> Unit,
        onProgressBar: (Int) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            var country: String = getCountryFromCity(context, cityName)

            if (country.isEmpty()) {
                // Keyword fallback when Geocoder fails
                val lower = cityName.lowercase()
                country = when {
                    lower.contains("india") || lower.contains("kolkata") || lower.contains("mumbai") ||
                    lower.contains("delhi") || lower.contains("bangalore") || lower.contains("chennai") ||
                    lower.contains("hyderabad") || lower.contains("pune") -> "India"
                    lower.contains("united kingdom") || lower.contains("london") || lower.contains("uk") -> "United Kingdom"
                    lower.contains("united states") || lower.contains("new york") || lower.contains("usa") -> "United States"
                    lower.contains("australia") || lower.contains("sydney") || lower.contains("melbourne") -> "Australia"
                    lower.contains("canada") || lower.contains("toronto") || lower.contains("vancouver") -> "Canada"
                    lower.contains("germany") || lower.contains("berlin") -> "Germany"
                    lower.contains("france") || lower.contains("paris") -> "France"
                    lower.contains("japan") || lower.contains("tokyo") -> "Japan"
                    lower.contains("china") || lower.contains("beijing") || lower.contains("shanghai") -> "China"
                    lower.contains("singapore") -> "Singapore"
                    lower.contains("dubai") || lower.contains("uae") -> "United Arab Emirates"
                    else -> "United Kingdom" // BBC feed is a reliable fallback
                }
                android.util.Log.d("NewsHandler", "Geocoder failed for '$cityName', fallback: $country")
            }

            val rssUrl: String = Utils.countryRss[country] ?: ""
            if (rssUrl.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "News Unavailable Now", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Only re-fetch if cache is empty or for a different country
            if (Utils.NewsList.isEmpty() || Utils.newsListCountry != country) {
                val stream = fetchRss(rssUrl)
                result = parseRss(stream, sourceNameFromUrl(rssUrl))
                Utils.NewsList = result
                Utils.newsListCountry = country
                android.util.Log.d("NewsHandler", "Fetched ${result.size} items for $country from $rssUrl")
            }

            withContext(Dispatchers.Main) {
                onLoaded(Utils.NewsList, 1)
                onProgressBar(1)
            }
        }
    }
}
