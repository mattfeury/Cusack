package com.mattfeury.cusack.services

import org.json.JSONObject
import com.mattfeury.cusack.util.Utils
import scala.xml.XML
import java.net.URI
import scala.xml.Node

case class MusicBrainzUriRelation(`type`:String, target:String) {
    lazy val uri = new URI(target)

    def hasHost(host:String) = uri.getHost() == host
    def hasType(`type`:String) = this.`type` == `type`
}

// TODO parse dat date
case class MusicBrainzReleaseGroup(id:String, `type`:String, title:String, firstReleaseDate:String, uriRelations:List[MusicBrainzUriRelation]) {

    def matchesTitle(otherTitle:String) = {
        val toPieces = { string:String => string.toLowerCase().replaceAll("""[^A-Za-z0-9 ]""", "").split(" ").toSet }
        val pieces = toPieces(title)
        val otherPieces = toPieces(otherTitle)

        pieces.forall(otherPieces.contains(_)) || otherPieces.forall(pieces.contains(_))
    }
}

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
                relation <- parseRelationList(relationList)
            } yield {
                relation
            }
        })
    }

    def getReleaseGroupsForArtist(artistId:String) : List[MusicBrainzReleaseGroup] = {
        makeUrlCall("release-group", Map(
            ("artist" -> artistId),
            ("type" -> "album|ep"),
            ("inc" -> "url-rels")
        ), response => {
            for {
                response <- response.toList
                metadata = XML.loadString(response)
                releaseGroupList <- (metadata \ "release-group-list")
                releaseGroup <- (releaseGroupList \ "release-group")
            } yield {
                MusicBrainzReleaseGroup(
                    id = (releaseGroup \ "@id").text,
                    `type` = (releaseGroup \ "@type").text,
                    title = (releaseGroup \ "title").text,
                    firstReleaseDate = (releaseGroup \ "first-release-date").text,
                    uriRelations = (releaseGroup \ "relation-list").flatMap(parseRelationList(_)).toList
                )
            }
        })
    }

    def parseRelationList(relationList:Node) : List[MusicBrainzUriRelation] = {
        (relationList \ "relation").map { relation =>
            MusicBrainzUriRelation(`type` = (relation \ "@type").text, target = (relation \ "target").text)
        }.toList
    }

    private def makeUrlCall[T](endpoint:String, params:Map[String, String], callback:Option[String]=>T) : T = {
        val url = API_URL + endpoint + "?" + Utils.makeQueryString(params)
        val json = GET(url)
        callback(json)
    }
}

object MusicBrainzService extends MusicBrainzService