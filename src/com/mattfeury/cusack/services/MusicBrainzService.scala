package com.mattfeury.cusack.services

import org.json.JSONObject
import com.mattfeury.cusack.util.Utils
import scala.xml.XML

case class MusicBrainzArtistInfo(id:String, name:String)
case class MusicBrainzUriRelation(`type`:String, target:String)

trait MusicBrainzService extends RestService {
    val API_URL = "http://musicbrainz.org/ws/2/"

    def getArtistId(artistName:String) : Option[String] = {
        makeUrlCall("artist", Map(
            ("query", artistName)
        ), response => {
            for {
                response <- response
                metadata = XML.loadString(response)
                artistList <- (metadata \ "artist-list").headOption
                artist <- (artistList \ "artist").headOption
                id = (artist \ "@id").text
            } yield {
                id
            }
        })
    }

    def getArtistUriRelations(artistId:String) : List[MusicBrainzUriRelation] = {
        makeUrlCall("artist/" + artistId, Map(
            ("inc", "url-rels")
        ), response => {
            for {
                response <- response.toList
                metadata = XML.loadString(response)
                artist <- (metadata \ "artist")
                relationList <- (artist \ "relation-list")
                relation <- relationList \ "relation"
            } yield {
                MusicBrainzUriRelation(`type` = (relation \ "@type").text, target = (relation \ "target").text)
            }
        })
    }

    private def makeUrlCall[T](endpoint:String, params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = API_URL + endpoint + "?" + Utils.makeQueryString(params)
        val json = GET(url)
        callback(json)
    }
}

object MusicBrainzService extends MusicBrainzService