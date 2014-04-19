package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.util.Utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class LastFmBioModule[A <: CusackReceiver with Context](receiver: A, attrs: AttributeSet = null) extends Module(receiver, attrs) {
  override def logo = Some(R.drawable.lastfm)

  override def selected = {
    for {
      song <- currentSong
      artist = song.artist
    } {
      val url = artist.lastFmArtistInfo.map { _.url } getOrElse {
        "https://www.google.com/search?" + Utils.makeQueryString(Map("q" -> song.artist.name))
      }

      receiver.openURIIntent(url)
    }
  }

  override def render(view: View) = {
      val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
      renderLogo(view)

      val text = {
          for {
              song <- currentSong
              artist = song.artist
              bio <- artist.lastFmArtistInfo.map(_.bio)
          } yield {
              bio
          }
      } getOrElse "-"

      moduleText.setText(text)
  }
}