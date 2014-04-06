package com.mattfeury.cusack.services

import java.net.URLEncoder

import scala.Option.option2Iterable
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.Map

import org.json.JSONArray
import org.json.JSONObject

import com.mattfeury.cusack.util.Utils

object WikipediaService {

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
                titles <- Utils.jsonArrayToList(jsonArray.getJSONArray(1))
            } yield {
                titles
            }
        })
    }

    def getExtractForTitle(title:String) : Option[String] = {
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
                keys = pages.keys()
                stringKeys = keys.asInstanceOf[java.util.Iterator[String]]
                page <- stringKeys.toList.headOption
                pageObject = pages.getJSONObject(page)
                extract = pageObject.get("extract")
            } yield {
                Utils.stripHtml(extract.toString())
            }
        })
    }

    def getExtractForKeyword(keyword:String) : Option[String]= {
        val titles = getTitlesForKeyword(keyword)

        // Scala list views are lazily evaluated
        val transformed = titles.view.flatMap(getExtractForTitle)
        transformed.find(_ != "")
    }

    private def makeUrlCall[T](params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = URL + "?" + (params + ("format" -> "json")).map {
            case (key, value) => URLEncoder.encode(key) + "=" + URLEncoder.encode(value)
        }.mkString("&")

        val json = RestService.GET(url)
        callback(json)
    }
}