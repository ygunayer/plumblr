package com.yalingunayer.plumblr.util

import java.io._
import java.nio.file.FileSystemException

import io.netty.handler.codec.http.HttpHeaders
import org.asynchttpclient._

import scala.concurrent.{ExecutionContext, Future}
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

  class FileDownloader(destination: File) extends AsyncHandler[File] {
    val fos = new FileOutputStream(destination)
    val output = new BufferedOutputStream(fos, 16384)

    override def onHeadersReceived(headers: HttpHeaders): AsyncHandler.State = AsyncHandler.State.CONTINUE;

    override def onStatusReceived(responseStatus: HttpResponseStatus): AsyncHandler.State = responseStatus.getStatusCode match {
      case 200 => AsyncHandler.State.CONTINUE
      case _ => AsyncHandler.State.ABORT
    }

    override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): AsyncHandler.State = {
      output.write(bodyPart.getBodyPartBytes)
      AsyncHandler.State.CONTINUE
    }

    override def onCompleted(): File = destination

    override def onThrowable(t: Throwable): Unit = Unit
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

  def downloadFile(url: String)(destination: File): Future[File] = {
    val req = new RequestBuilder().setUrl(url)

    val downloader = new FileDownloader(destination)

    val result = asyncHttpClient
      .executeRequest(req, downloader)
      .toCompletableFuture

    FutureConverters.toScala(result)
  }
}
