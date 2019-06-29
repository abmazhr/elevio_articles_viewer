package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{ArticleSearchResult, GetArticleByKeyword}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleSearchResultObj
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleSearchResultError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._

final case class GetArticleWithKeywordClient(
    keyword: String,
    currentResult: ArticleSearchResult = Right(List.empty)
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(keyword: String) extends GetArticleByKeyword {
    val httpRequest: Request[String, Nothing] =
      sttp
        .headers(config.httpClientHeaders)
        .readTimeout(30.second)
        .get(uri"${config.apiEndpoint}/search/en?query=$keyword")

    override def process: ArticleSearchResult = {
      try {
        val response: Id[Response[String]] = httpRequest.send()
        val doc: Json                      = parse(response.unsafeBody).getOrElse(Json.Null)
        val cursor: HCursor                = doc.hcursor
        val searchResult                   = cursor.downField("results").as[List[ArticleSearchResultObj]]

        searchResult.fold(
          _ => Left(HttpFetchingArticleSearchResultError()),
          searchResult => Right(searchResult)
        )
      } catch {
        case _: Throwable => Left(HttpFetchingArticleSearchResultError())
      }
    }
  }

  def process = GetArticleWithKeywordClient(keyword, Implementation(keyword).process)
}
