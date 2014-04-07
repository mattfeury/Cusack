package com.mattfeury.cusack.services

import java.net.URLEncoder

import scala.Option.option2Iterable
import scala.collection.JavaConversions.asScalaIterator

import org.json.JSONArray
import org.json.JSONObject

import com.mattfeury.cusack.util.Utils

case class WikipediaPageInfo(title:String, extract:String) {
    def getUrl() = WikipediaService.getUrlForTitle(title)
}

trait WikipediaService {

    final lazy val URL = "http://en.wikipedia.org/w/api.php"

    def getUrlForTitle(title:String) = "http://en.wikipedia.org/wiki/" + title

    def getTitlesForKeyword(keyword:String) : List[String] = {
        makeUrlCall(Map(
            ("action", "opensearch"),
            ("search", keyword)
        ), response => {
            for {
                response <- response.toList
                jsonArray = new JSONArray(response)
                titles <- Utils.jsonArrayToList[String](jsonArray.getJSONArray(1))
            } yield {
                titles
            }
        })
    }

    def getPageInfoForTitle(title:String) : Option[WikipediaPageInfo] = {
        makeUrlCall(Map(
            ("action", "query"),
            ("prop", "extracts"),
            ("exintro", ""),
            ("titles", title)
        ), response => {
            for {
                response <- response
                json = new JSONObject(response)
                query = json.getJSONObject("query")
                pages = query.getJSONObject("pages")
                page <- pages.keys().asInstanceOf[java.util.Iterator[String]].toList.headOption
                pageObject = pages.getJSONObject(page)
                extract = pageObject.get("extract")
                title = pageObject.get("title")
            } yield {
                WikipediaPageInfo(title = title.toString, extract = Utils.stripHtml(extract.toString()))
            }
        })
    }

    def getPageInfoForKeyword(keyword:String) : Option[WikipediaPageInfo]= {
        val titles = getTitlesForKeyword(keyword)

        // Scala list views are lazily evaluated
        val transformed = titles.view.flatMap(getPageInfoForTitle)
        transformed.find(_.extract != "")
    }

    private def makeUrlCall[T](params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = URL + "?" + Utils.makeQueryString(params + ("format" -> "json"))

        val json = RestService.GET(url)
        callback(json)
    }
}

object WikipediaService extends WikipediaService