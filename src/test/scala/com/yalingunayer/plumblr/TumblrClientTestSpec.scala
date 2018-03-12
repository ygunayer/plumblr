package com.yalingunayer.plumblr;

import java.util.Date

import com.yalingunayer.plumblr.domain.Domain.{BlogPost, BlogSearch, Photo}
import com.yalingunayer.plumblr.util.TumblrClient
import org.scalatest.FlatSpec

import scala.io.Source

class TumblrClientTestSpec extends FlatSpec {
  "sanitizeResponse" should "correctly sanitize input" in {
    val input = Source.fromResource("response-input-1.txt").mkString
    val expected = Source.fromResource("response-output-1.json").mkString.trim
    val actual = TumblrClient.sanitizeResponseBody(input)
    assert(actual == expected)
  }

  "parseBlogSearch" should "correctly parse a response" in {
    val input = Source.fromResource("response-output-1.json").mkString

    val expected = BlogSearch(
      List(
        BlogPost("105877331682", "https://someunimaginativename.tumblr.com/post/105877331682", new Date(1419266400L * 1000), List(
          Photo("https://78.media.tumblr.com/7ae0b797f9c7f35dfb36281f760ca6b2/tumblr_ngzsyqFWJE1u6u2voo1_500.png")
        ))
      )
    )

    val actual = TumblrClient.parseBlogSearch(input)
    assert(actual == expected)
  }
}
