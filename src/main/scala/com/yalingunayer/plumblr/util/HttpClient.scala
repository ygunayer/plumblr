package com.yalingunayer.plumblr.util

import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.AsyncHttpClientConfig
import org.asynchttpclient.RequestBuilder
import scala.concurrent.Future
import scala.compat.java8.FutureConverters

object HttpClient {
  lazy val defaultConfig: AsyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
    .setMaxConnectionsPerHost(200)
    .setMaxConnections(400)
    .setRequestTimeout(30000)
    .build()

  trait Parser[A] {
    def parse(s: String): A
  }

  implicit object StringParser extends Parser[String] {
    def parse(s: String): String = s
  }

  def apply(): HttpClient = apply(defaultConfig)
  def apply(config: AsyncHttpClientConfig): HttpClient = new HttpClient(config)
}

class HttpClient(config: AsyncHttpClientConfig) {
  import HttpClient._

  lazy val asyncHttpClient = new DefaultAsyncHttpClient()

  def doRequest[A](req: RequestBuilder)(implicit parser: Parser[A]): Future[A] = {
    val result = asyncHttpClient
      .executeRequest(req)
      .toCompletableFuture
      .thenApply[A](response => parser.parse(response.getResponseBody()))

    FutureConverters.toScala(result)
  }
}
