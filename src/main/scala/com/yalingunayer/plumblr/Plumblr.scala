package com.yalingunayer.plumblr;

import java.io.File
import java.nio.file.{Path, Paths}

import akka.NotUsed
import akka.actor.ActorSystem
import com.yalingunayer.plumblr.domain.Messages
import akka.stream.scaladsl.Source
import com.yalingunayer.plumblr.util.{HttpClient, TumblrClient}
import akka.stream.scaladsl.Sink
import akka.stream.ActorMaterializer

import scala.util.Success
import scala.util.Failure
import akka.stream.scaladsl.Keep
import com.yalingunayer.plumblr.domain.Domain.{BlogPost, BlogSearch, Media}
import me.tongfei.progressbar.ProgressBar

import scala.concurrent.Future

object Plumblr {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("plumblr-main")
    implicit val mat = ActorMaterializer()
    implicit val ec = system.dispatcher

    val parallelism = 8

    val progressBar = new ProgressBar("plumblr", 0)

    val filenameRegex = "\\/?([\\.\\w]+)$".r

    lazy val client = TumblrClient()
    lazy val httpClient = HttpClient()

    args.headOption match {
      case Some(blogName) => {
        progressBar.start()

        val blogSearches: Source[BlogSearch, NotUsed] = Source.repeat(blogName)
          .zip(Source.fromIterator(() => Iterator.from(1)))
          .mapAsync(100)({
            case (a: String, b: Int) => {
              client.search(a, Some(b * 20))
            }
          })
          .takeWhile(_.posts.nonEmpty)

        val blogPosts: Source[BlogPost, NotUsed] = blogSearches
          .mapConcat(a => identity(a.posts.toList))

        val blogMediaWithPosts: Source[(BlogPost, Media), NotUsed] = blogPosts
            .mapConcat(post => post.media.map(media => (post, media)).to[scala.collection.immutable.Iterable])

        val downloads = blogMediaWithPosts
          .zipWithIndex
          .mapAsync(parallelism)({
            case ((post, media), idx) => {
              val filename = filenameRegex.findFirstMatchIn(media.downloadUrl).head.group(1)
              val targetPath = Paths.get(System.getProperty("user.home"), "plumblr", blogName, filename).toString
              val destination = new File(targetPath)

              destination.getParentFile.mkdirs

              progressBar.maxHint(Math.max(idx, progressBar.getMax))
              progressBar.setExtraMessage(destination.getAbsolutePath)

              httpClient.downloadFile(media.downloadUrl)(destination)
            }
          })

        val flow = downloads
          //.map(_.url)
          .toMat(Sink.foreach(_ => progressBar.step))(Keep.right)
          .run()

        flow.onComplete {
          case Success(result) => {
            println("Successfully completed downloading")
            system.terminate()
          }
          case Failure(t) => {
            System.err.println(f"Failed due to $t")
            system.terminate()
          }
        }

        system.whenTerminated.onComplete {
          case Failure(t) => {
            System.err.println(f"Uncaught error $t")
            System.exit(-1)
          }
          case _ => System.exit(0)
        }
      }
      case _ => {
        System.err.println("No blog name specified")
        System.exit(-1)
      }
    }
  }
}
