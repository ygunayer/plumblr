package com.yalingunayer.plumblr.util

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.asynchttpclient.RequestBuilder
import com.yalingunayer.plumblr.domain.Domain._

object TumblrClient {
  def apply(): TumblrClient = new TumblrClient()
}

class TumblrClient {
  lazy val client = HttpClient()

  import spray.json._
  import com.yalingunayer.plumblr.domain.Domain.BlogParserSupport._

  def search(blogName: String, start: Option[Int])(implicit ec: ExecutionContext): Future[Seq[BlogPost]] = {
    val pageParam = start.map(x => f"?start=$x").getOrElse("")
    val request = new RequestBuilder().setUrl(f"https://${blogName}.tumblr.com/api/read/json${pageParam}")

    client.doRequest(request)
      .map(body => body.substring("var tumblr_api_read = ".length, body.length() - 2).parseJson.convertTo[BlogSearch])
      .map(_.posts)
  }
}
