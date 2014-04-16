package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.TextView

class TwitterModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
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
        val moduleImage = view.findViewById(R.id.moduleImage)

        textView.setTextAppearance(receiver, R.style.boldText)
        moduleImage.getParent.asInstanceOf[ViewManager].removeView(moduleImage)

        val text = {
            for {
                song <- currentSong
                twitterInfo <- song.artist.twitterInfo
                handle = twitterInfo.handle
                latestTweet = twitterInfo.latestTweet
            } yield {
                s"$latestTweet\n\n-$handle"
            }
        } getOrElse {
            "It's twitter, yo"
        }

        textView.setText(text)
    }
}