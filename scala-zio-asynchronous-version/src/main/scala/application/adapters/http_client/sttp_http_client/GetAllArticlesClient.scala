package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{AllArticlesResult, GetAllArticles}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleRef
import domain.entity.Config
import domain.entity.Error.HttpFetchingAllArticlesError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import zio.UIO

import scala.concurrent.duration._

final case class GetAllArticlesClient(
    currentPage: Int = 1,
    currentResult: AllArticlesResult = UIO.succeed(Right(List.empty))
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(currentPage: Int) extends GetAllArticles {
    override def process: AllArticlesResult =
      for {
        httpRequest <- UIO.effectTotal(
          sttp
            .headers(config.httpClientHeaders)
            .readTimeout(30.second)
            .get(uri"${config.apiEndpoint}/articles?page=$currentPage")
        )
        response     <- UIO.effectTotal(httpRequest.send())
        doc          <- UIO.effectTotal(parse(response.unsafeBody).getOrElse(Json.Null))
        cursor       <- UIO.effectTotal(doc.hcursor)
        articlesJson <- UIO.effectTotal(cursor.downField("articles"))
        articlesObj <- articlesJson
          .as[List[ArticleRef]]
          .fold(
            _ => UIO.effectTotal(Left(HttpFetchingAllArticlesError())),
            articles => UIO.effectTotal(Right(articles))
          )
      } yield articlesObj
  }

  def process      = GetAllArticlesClient(currentPage, Implementation(currentPage).process)
  def nextPage     = GetAllArticlesClient(currentPage + 1, Implementation(currentPage + 1).process)
  def previousPage = GetAllArticlesClient(currentPage - 1, Implementation(currentPage - 1).process)
}
