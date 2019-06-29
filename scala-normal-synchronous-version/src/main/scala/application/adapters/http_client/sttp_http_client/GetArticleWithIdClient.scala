package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{ArticleObject, GetArticleById}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleObj
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._

final case class GetArticleWithIdClient(
    id: Int,
    currentResult: ArticleObject = Left(HttpFetchingArticleError())
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(id: Int) extends GetArticleById {
    val httpRequest: Request[String, Nothing] =
      sttp
        .headers(config.httpClientHeaders)
        .readTimeout(30.second)
        .get(uri"${config.apiEndpoint}/articles/$id")

    override def process: ArticleObject = {
      try {
        val response: Id[Response[String]]       = httpRequest.send()
        val doc: Json                            = parse(response.unsafeBody).getOrElse(Json.Null)
        val cursor: HCursor                      = doc.hcursor
        val articles: Decoder.Result[ArticleObj] = cursor.downField("article").as[ArticleObj]

        articles.fold(
          _ => Left(HttpFetchingArticleError()),
          article => Right(article)
        )
      } catch {
        case _: Throwable => Left(HttpFetchingArticleError())
      }
    }
  }

  def process = GetArticleWithIdClient(id, Implementation(id).process)
}
