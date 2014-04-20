package com.mattfeury.cusack.analytics

import scala.collection.JavaConversions.mapAsJavaMap

import org.json.JSONObject

import com.mattfeury.cusack.Cusack
import com.mixpanel.android.mpmetrics.MixpanelAPI

import android.app.Activity
import android.util.Log

object Mixpanel {
    val token = "sup?"
    var api:Option[MixpanelAPI] = None

    def setup(activity:Activity) = {
        api = Some(MixpanelAPI.getInstance(activity, token))
    }

    def flush() = makeCall(_.flush())

    def track(eventName:String, properties:Map[String, String] = Map()) = {
        makeCall(_.track(eventName, new JSONObject(properties)))
    }

    def identify(properties:Map[String, String]) = {
        makeCall(_.registerSuperProperties(new JSONObject(properties)))
    }

    private def makeCall(fn:MixpanelAPI=>Unit) = {
        try {
            api.foreach(fn(_))
        } catch {
            case e:Exception =>
                Log.e(Cusack.TAG, "Error making analytics call " + e.getMessage())
                e.printStackTrace()
        }
    }
}