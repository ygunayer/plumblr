package com.yalingunayer.plumblr.domain

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object Domain {
  case class BlogPost(id: String, url: String, date: String)
  case class BlogSearch(posts: Seq[BlogPost])

  object BlogParserSupport extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val blogPostFormat = jsonFormat3(BlogPost)
    implicit val blogSearchFormat = jsonFormat1(BlogSearch)
  }
}
