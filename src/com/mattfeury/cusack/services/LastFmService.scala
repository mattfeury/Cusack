package com.mattfeury.cusack.services

import com.mattfeury.cusack.util.Utils
import org.json.JSONObject
import android.graphics.Bitmap

case class LastFmArtistInfo(name:String, imageUrl:String) {
    val imageBitmap = Utils.downloadBitmap(imageUrl)
}

trait LastFmService extends RestService {
    val API_URL = "http://ws.audioscrobbler.com/2.0/"
    val API_KEY = "67d61a5eb3daa18ea6af3e7927dd3c3c"

    def getArtistInfo(artistName:String) : Option[LastFmArtistInfo] = {
        makeUrlCall(Map(
            ("method", "artist.getinfo"),
            ("artist", artistName)
        ), response => {
            for {
                response <- response
                json = new JSONObject(response)
                artist = json.getJSONObject("artist")
                images = Utils.jsonArrayToList[JSONObject](artist.getJSONArray("image"))
            } yield {
                val largestImage = images.collectFirst {
                    case json if json.get("size") == "mega" => json.get("#text").toString()
                } getOrElse ""

                LastFmArtistInfo(name = artist.get("name").toString(), imageUrl = largestImage)
            }
        })
    }

    private def makeUrlCall[T](params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = API_URL + "?" + Utils.makeQueryString(params ++ Map(("format" -> "json"), ("api_key" -> API_KEY)))
        val json = GET(url)
        callback(json)
    }
}

object LastFmService extends LastFmService