package com.mattfeury.cusack.services

import scala.collection.JavaConverters.asScalaBufferConverter

import twitter4j.Status
import twitter4j.TwitterFactory

case class TwitterInfo(handle:String, latestTweet:String)

trait TwitterService {

    lazy val twitter4j = TwitterFactory.getSingleton() 

    def getLatestTweet(handle:String) : Option[String] = {
        val statuses = twitter4j.getUserTimeline(handle)

        // Find the best status by the one with the most favs + RTs and is not a reply/retweet
        val statusScoreFor = { s:Status => s.getFavoriteCount() + s.getRetweetCount() }
        val shouldDisallow = { s:Status => s.isRetweet() || s.getText().charAt(0) == '@' }

        val status:Option[Status] = statuses.asScala.foldLeft(None:Option[Status]) { (best, next) =>
            if (best.map(! shouldDisallow(next) && statusScoreFor(next) > statusScoreFor(_)).getOrElse(true)) {
                Some(next)
            } else {
                best
            }
        }

        status.map(_.getText())
    }
}

object TwitterService extends TwitterService