package eu.kanade.tachiyomi.extension.en.nhentai

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class NHentai : ParsedHttpSource() {

    override val name = "nHentai"
    override val baseUrl = "https://nhentai.net"
    override val lang = "en"
    override val supportsLatest = true

    // --- Popular / Latest ---
    override fun popularMangaSelector() = ".gallery"
    override fun latestUpdatesSelector() = ".gallery"

    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/search/?q=english&page=$page", headers)
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/?page=$page", headers)

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        url = element.select("a").attr("href")
        title = element.select(".caption").text()
        thumbnail_url = element.select("img").attr("data-src")
    }

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun popularMangaNextPageSelector() = "a.next"
    override fun latestUpdatesNextPageSelector() = "a.next"

    // --- Search ---
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search/?q=$query&page=$page", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // --- Manga Details ---
    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.select("h1.title").text()
        artist = document.select(".tag-container:contains(Artists) .name").text()
        genre = document.select(".tag-container:contains(Tags) .name").joinToString { it.text() }
        description = "Pages: " + document.select(".tag-container:contains(Pages) .name").text()
        thumbnail_url = document.select("#cover img").attr("data-src")
    }

    // --- Chapters (nHentai galleries are technically 1 chapter) ---
    override fun chapterListSelector() = "html" 
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        url = element.baseUri()
        name = "Chapter 1"
        date_upload = System.currentTimeMillis()
    }

    // --- Page List (The Images) ---
    override fun pageListParse(document: Document): List<Page> {
        val mediaId = document.select("#cover img").attr("data-src").split("/")[4]
        val pageCount = document.select(".tag-container:contains(Pages) .name").text().toInt()

        return (1..pageCount).map { i ->
            Page(i, "", "https://i.nhentai.net/galleries/$mediaId/$i.jpg")
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")
}
