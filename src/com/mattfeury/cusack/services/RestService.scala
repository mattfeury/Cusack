package com.mattfeury.cusack.services

import java.io.InputStream
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.io.BufferedReader
import org.json.JSONObject
import android.util.Log
import java.io.InputStreamReader

trait RestService {
    // TODO rewrite in scala. this is straight java yo
    def GET(url:String) : Option[String] = {
        Log.d("CUSACK", "Getting url: " + url)

        val httpclient = new DefaultHttpClient()
        val get = new HttpGet(url)
        get.setHeader("User-Agent", "Cusack Android App: mattfeury.com")
        val httpResponse = httpclient.execute(get)

        httpResponse.getEntity().getContent() match {
            case stream if stream != null =>
                val textResponse = convertInputStreamToString(stream)
                Log.d("CUSACK", "URL response: " + url + "\n\n" + textResponse)

                Some(textResponse)
            case _ =>
                None
        }
    }

    private def convertInputStreamToString(inputStream:InputStream) : String = {
        scala.io.Source.fromInputStream(inputStream).getLines().mkString("\n")
    }
}

object RestService extends RestService