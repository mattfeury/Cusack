package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.TextView
import android.widget.ImageView
import android.text.util.Linkify

class TwitterModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {

    override def logo = Some(R.drawable.twitter_logo)

    override def selected = {
        for {
            song <- currentSong
            twitterInfo <- song.artist.twitterInfo
            handle = twitterInfo.handle
        } {
            receiver.openURIIntent("https://twitter.com/" + handle)
        }
    }

    override def render(view:View) = {
        val textView = view.findViewById(R.id.moduleText).asInstanceOf[TextView]

        val text = {
            for {
                song <- currentSong
                twitterInfo <- song.artist.twitterInfo
                handle = twitterInfo.handle
                latestTweet = twitterInfo.latestTweet
            } yield {
                s"$latestTweet\n\n@$handle"
            }
        } getOrElse {
            "-"
        }

        textView.setText(text)
        Linkify.addLinks(textView, Linkify.ALL)
    }
}