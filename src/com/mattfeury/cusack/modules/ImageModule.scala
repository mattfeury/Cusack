package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

class ImageModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
    override def selected = {}

    override val templateId = R.layout.image_module_view

    override def render(view:View) = {
        for {
            song <- currentSong
            artist = song.artist
            lastFmInfo <- artist.lastFmArtistInfo
            bitmap <- lastFmInfo.imageBitmap
        } {
            val imageView = view.findViewById(R.id.moduleImage).asInstanceOf[ImageView]
            imageView.setImageBitmap(bitmap)
        }
    }
}