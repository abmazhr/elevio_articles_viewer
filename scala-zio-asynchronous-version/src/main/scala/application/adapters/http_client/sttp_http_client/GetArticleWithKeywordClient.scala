package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{ArticleSearchResult, GetArticleByKeyword}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleSearchResultObj
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleSearchResultError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import zio.UIO

import scala.concurrent.duration._

final case class GetArticleWithKeywordClient(
    keyword: String,
    currentResult: ArticleSearchResult = UIO.effectTotal(Right(List.empty))
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(keyword: String) extends GetArticleByKeyword {
    override def process: ArticleSearchResult = {
      for {
        httpRequest <- UIO.effectTotal(
          sttp
            .headers(config.httpClientHeaders)
            .readTimeout(30.second)
            .get(uri"${config.apiEndpoint}/search/en?query=$keyword")
        )
        response         <- UIO.effectTotal(httpRequest.send())
        doc              <- UIO.effectTotal(parse(response.unsafeBody).getOrElse(Json.Null))
        cursor           <- UIO.effectTotal(doc.hcursor)
        searchResultJson <- UIO.effectTotal(cursor.downField("results"))
        searchResultObj <- searchResultJson
          .as[List[ArticleSearchResultObj]]
          .fold(
            _ => UIO.effectTotal(Left(HttpFetchingArticleSearchResultError())),
            result => UIO.effectTotal(Right(result))
          )
      } yield searchResultObj
    }
  }

  def process = GetArticleWithKeywordClient(keyword, Implementation(keyword).process)
}
