package com.yalingunayer.plumblr.domain

import java.util.Date

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object Domain {
  trait Media {
    val downloadUrl: String
  }

  case class Photo(url: String) extends Media { val downloadUrl: String = url }
  case class Video(url: String) extends Media { val downloadUrl: String = url }
  case class Gallery(photos: Seq[Photo])

  case class BlogPost(id: String, url: String, date: Date, media: Seq[Media])
  case class BlogSearch(posts: Seq[BlogPost])

  object Parsers {
    val videoIdPattern = "(tumblr_[A-Za-z\\d]+)".r
    val photoFields: Vector[String] = Vector(1280, 500, 400, 250, 100, 75).map(x => f"photo-url-$x")

    def extractVideoSource(playerString: String): Option[String] = videoIdPattern.findFirstIn(playerString).map(id => f"https://vtt.tumblr.com/$id.mp4")

    def parsePhoto(json: JsValue): Option[Photo] = {
      val jsonFields = json.asJsObject.fields

      photoFields
        .find(jsonFields.contains)
        .map(jsonFields)
        .map(_.toString.replaceFirst("^\"", "").replaceFirst("\"$", ""))
        .map(Photo)
    }

    def parseVideo(json: JsValue): Option[Video] = json.asJsObject.fields.get("video-player").headOption.map(_.toString).flatMap(extractVideoSource).map(Video) // TODO parse properly

    def parseBlogMedia(json: JsValue): Seq[Media] = {
      val obj = json.asJsObject
      val mainPhoto = Parsers.parsePhoto(json)

      if (obj.fields.contains("video-player")) parseVideo(json).toSeq
      else {
        obj.getFields("photos") match {
          case Seq(JsArray(photos)) if photos.length > 0 => photos.flatMap(parsePhoto).toSeq
          case _ => mainPhoto.toSeq
        }
      }
    }

    def parseBlogPost(json: JsValue): BlogPost = json.asJsObject.getFields("id", "url", "unix-timestamp") match {
      case Seq(JsString(id), JsString(url), JsNumber(timestamp)) => {
        val date = new Date(timestamp.toLong * 1000)
        val media = Parsers.parseBlogMedia(json)
        BlogPost(id, url, date, media)
      }
    }
  }

  object BlogJsonProtocol extends DefaultJsonProtocol {
    implicit object PhotoFormat extends RootJsonFormat[Photo] with SprayJsonSupport {
      override def write(obj: Photo): JsValue = ???
      override def read(json: JsValue): Photo =  Parsers.parsePhoto(json).head // TODO handle missing case
    }

    implicit object VideoFormat extends RootJsonFormat[Video] with SprayJsonSupport {
      override def write(obj: Video): JsValue = ???
      override def read(json: JsValue): Video = Parsers.parseVideo(json).head // TODO handle missing case
    }

    implicit object BlogPostFormat extends RootJsonFormat[BlogPost] with SprayJsonSupport {
      override def write(obj: BlogPost): JsValue = ???
      override def read(json: JsValue): BlogPost = Parsers.parseBlogPost(json)
    }

    implicit val blogSearchFormat = jsonFormat1(BlogSearch)
  }
}
