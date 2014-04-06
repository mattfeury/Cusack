package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.Song
import com.mattfeury.cusack.services.WikipediaService

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

class WikipediaModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
    var extract:Option[String] = None

    override def selected = {
    }

    override def songChanged(song:Song) = {
        val oldSong = this.currentSong
        super.songChanged(song)

        oldSong match {
            // Only refetch artist bio if it's a different artist. Let's be nice to wikipedia
            case Some(lastSong) if lastSong.artist != song.artist =>
            case None =>
                val task = new GetExtractTask()
                task.execute(song)

            case _ =>
        }
    }

    override def render(view:View) = {
        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
        val text = extract.getOrElse("-")
        moduleText.setText(text)
    }

    class GetExtractTask extends AsyncTask[AnyRef, Unit, Unit] {
        // AnyRef required due to scala/android bug: http://piotrbuda.eu/2012/12/scala-and-android-asynctask-implementation-problem.html
        override def doInBackground(song:AnyRef*) = {
            extract = WikipediaService.getExtractForKeyword(song.toList.head.asInstanceOf[Song].artist)
        }

        override def onPostExecute(unit:Unit) : Unit = {
            receiver.redraw()
        }
    }
}