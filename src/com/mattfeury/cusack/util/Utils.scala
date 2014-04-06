package com.mattfeury.cusack.util

import org.json.JSONArray

import android.text.Html

object Utils {
    def jsonArrayToList(json:JSONArray) : List[String] = {
        var list = List[String]()
        val length = json.length

        for (i <- 0 until length) {
            list = list :+ json.get(i).toString()
        }

        list
    }

    def stripHtml(html:String) : String = Html.fromHtml(html).toString
}