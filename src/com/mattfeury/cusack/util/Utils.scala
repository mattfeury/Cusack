package com.mattfeury.cusack.util

import java.net.URLEncoder
import org.json.JSONArray
import android.text.Html
import android.graphics.Bitmap
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import android.util.Log
import org.apache.http.HttpStatus
import java.net.URL
import java.net.HttpURLConnection
import android.graphics.BitmapFactory

object Utils {
    def jsonArrayToList[K](json:JSONArray) : List[K] = {
        var list = List[K]()
        val length = json.length

        for (i <- 0 until length) {
            list = list :+ (json.get(i).asInstanceOf[K])
        }

        list
    }

    def stripHtml(html:String) : String = Html.fromHtml(html).toString.replaceAll("""\n*$""", "")

    def makeQueryString(params:Map[String, String]) : String = params.map {
        case (key, value) => URLEncoder.encode(key) + "=" + URLEncoder.encode(value)
    }.mkString("&")

    def downloadBitmap(source:String) : Option[Bitmap] = {
        try {
            val url = new URL(source)
            val connection = url.openConnection().asInstanceOf[HttpURLConnection]
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream

            Some(BitmapFactory.decodeStream(input))
        } catch {
            case e:Exception => {
                e.printStackTrace()
                None
            }
        }
    }
}