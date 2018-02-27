package com.yalingunayer.plumblr;

import akka.actor.ActorSystem
import com.yalingunayer.plumblr.domain.Messages
import akka.stream.scaladsl.Source
import com.yalingunayer.plumblr.util.TumblrClient
import akka.stream.scaladsl.Sink
import akka.stream.ActorMaterializer
import scala.util.Success
import scala.util.Failure
import akka.stream.scaladsl.Keep

object Plumblr {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("plumblr-main")
    implicit val mat = ActorMaterializer()
    implicit val ec = system.dispatcher

    lazy val client = TumblrClient()

    args.headOption match {
      case Some(blogName) => {
        val flow = Source.repeat(blogName)
          .zip(Source.fromIterator(() => Iterator.from(20)))
          .mapAsync(8)({
            case (a: String, b: Int) => {
              client.search(a, Some(b))
            }
          })
          .takeWhile(_.nonEmpty)
          .mapConcat(a => identity(a.toList))
          .map(_.url)
          .toMat(Sink.foreach(println))(Keep.right)
          .run()

        flow.onComplete {
          case Success(result) => println("Successfully completed downloading")
          case Failure(t) => System.err.println(f"Failed due to $t")
        }
      }
      case _ => {
        System.err.println("No blog name specified")
        System.exit(-1)
      }
    }
  }
}
