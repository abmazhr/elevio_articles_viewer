package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{AllArticlesResult, GetAllArticles}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleRef
import domain.entity.Config
import domain.entity.Error.HttpFetchingAllArticlesError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._

final case class GetAllArticlesClient(
    currentPage: Int = 1,
    currentResult: AllArticlesResult = Right(List.empty)
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(currentPage: Int) extends GetAllArticles {
    val httpRequest: Request[String, Nothing] =
      sttp
        .headers(config.httpClientHeaders)
        .readTimeout(30.second)
        .get(uri"${config.apiEndpoint}/articles?page=$currentPage")

    override def process: AllArticlesResult = {
      try {
        val response: Id[Response[String]]             = httpRequest.send()
        val doc: Json                                  = parse(response.unsafeBody).getOrElse(Json.Null)
        val cursor: HCursor                            = doc.hcursor
        val articles: Decoder.Result[List[ArticleRef]] = cursor.downField("articles").as[List[ArticleRef]]

        articles.fold(
          _ => Left(HttpFetchingAllArticlesError()),
          values => Right(values)
        )
      } catch {
        case _: Throwable => Left(HttpFetchingAllArticlesError())
      }
    }
  }

  def process      = GetAllArticlesClient(currentPage, Implementation(currentPage).process)
  def nextPage     = GetAllArticlesClient(currentPage + 1, Implementation(currentPage + 1).process)
  def previousPage = GetAllArticlesClient(currentPage - 1, Implementation(currentPage - 1).process)
}
