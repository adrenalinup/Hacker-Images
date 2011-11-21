package models

import play.api._
import play.api.libs.concurrent._
import play.api.cache.BasicCache

import org.jsoup.Jsoup
import org.jsoup.nodes._

import collection.JavaConversions._
import java.net.URI

trait ImageExtractor {
  def getImageUrl(pageUrl:String): Promise[Option[String]]
}

object ScreenshotExtractor extends ImageExtractor {
  def getImageUrl(pageUrl:String): Promise[Option[String]] = {
    Promise.pure(Some( "http://immediatenet.com/t/fs?Size=1024x1024&URL="+pageUrl) )
  }
}

object MostRelevantPageImageExtractor extends ImageExtractor {
  val cache = new BasicCache()
  def cacheKey(link:String) = "models.MostRelevantPageImageExtractor.cacheKey_for_"+link
  val expirationSeconds = 5*60

  def getImageUrl(url:String): Promise[Option[String]] = {
    cache.get[Option[String]](cacheKey(url)).map(Promise.pure(_)).getOrElse({
      Logger.debug("MostRelevantPageImageExtractor.getImageUrl("+url+")")
      WS.url(url).get().map(html => {
        val src = Jsoup.parse(html.getResponseBody()).select("img").headOption.map(
          image => new URI(url).resolve(image.attr("src")).toString()
        )
        cache.set(cacheKey(url), src)
        src
      })
    })
  }
}

