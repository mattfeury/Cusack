package com.mattfeury.cusack.util

import org.json.JSONArray

import android.text.Html

object Utils {
    def jsonArrayToList[K](json:JSONArray) : List[K] = {
        var list = List[K]()
        val length = json.length

        for (i <- 0 until length) {
            list = list :+ (json.get(i).asInstanceOf[K])
        }

        list
    }

    def stripHtml(html:String) : String = Html.fromHtml(html).toString
}