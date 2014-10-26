package com.mattfeury.cusack.services

import java.net.URLEncoder
import scala.Option.option2Iterable
import scala.collection.JavaConversions.asScalaIterator
import org.json.JSONArray
import org.json.JSONObject
import com.mattfeury.cusack.util.Utils
import java.net.URLDecoder

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
            ("action", "mobileview"),
            ("prop", "text"),
            ("sections", "0"), // we can change this!
            ("page", title)
        ), response => {
            for {
                response <- response
                json = new JSONObject(response)
                mobileview = json.getJSONObject("mobileview")
                section <- Utils.jsonArrayToList[JSONObject](mobileview.getJSONArray("sections")).headOption
                extract = section.get("text")
            } yield {
                WikipediaPageInfo(title = title.toString, extract = Utils.stripHtml(stripExtract(extract.toString())))
            }
        })
    }

    def getPageTitleFromUrl(url:String) : String = {
        // Hmm...
        URLDecoder.decode(url.split("/").last)
    }

    def getPageInfoForKeyword(keyword:String) : Option[WikipediaPageInfo]= {
        val titles = getTitlesForKeyword(keyword)

        // Scala list views are lazily evaluated
        val transformed = titles.view.flatMap(getPageInfoForTitle)
        transformed.find(_.extract != "")
    }

    def getImageFilenamesForTitle(title:String) : List[String] = {
        makeUrlCall(Map(
            ("action", "parse"),
            ("prop", "text|images"),
            ("page", title)
        ), response => {
            for {
                response <- response.toList
                json = new JSONObject(response)
                parse = json.getJSONObject("parse")
                image <- Utils.jsonArrayToList(parse.getJSONArray("images"))
            } yield {
                image
            }
        })
    }

    private def stripLinksAndRefs(input:String): String = input.replaceAll("\\<a.*?>|</a>|\\[.*?\\]", "")
    private def stripImageTags(input:String) : String = input.replaceAll("\\<img.*?/>", "")
    private def stripTablesAndDivs(input:String) : String = input.replaceAll("(?s:\\<div.*?</div>|\\<table.*?</table>)", "")

    private def stripExtract(input:String) = stripLinksAndRefs(stripImageTags(stripTablesAndDivs(input))) // Eww stop this 

    private def makeUrlCall[T](params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = URL + "?" + Utils.makeQueryString(params + ("format" -> "json"))

        val json = RestService.GET(url)
        callback(json)
    }
}

object WikipediaService extends WikipediaService