package com.yalingunayer.plumblr.util

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.asynchttpclient.RequestBuilder
import com.yalingunayer.plumblr.domain.Domain._

object TumblrClient {
  import spray.json._
  import com.yalingunayer.plumblr.domain.Domain.BlogJsonProtocol._

  def sanitizeResponseBody(body: String): String = body.trim.replaceFirst("var tumblr_api_read = ", "").replaceFirst(";$", "")

  def parseBlogSearch(body: String): BlogSearch = TumblrClient.sanitizeResponseBody(body).parseJson.convertTo[BlogSearch]

  def apply(): TumblrClient = new TumblrClient()
}

class TumblrClient {
  lazy val client = HttpClient()

  def search(blogName: String, start: Option[Int])(implicit ec: ExecutionContext): Future[BlogSearch] = {
    val pageParam = start.map(x => f"?start=$x").getOrElse("")
    val request = new RequestBuilder().setUrl(f"https://${blogName}.tumblr.com/api/read/json${pageParam}")

    client.doRequest(request).map(TumblrClient.parseBlogSearch)
  }
}
