package application.adapters.http_client.sttp_http_client

import application.adapters.http_client.GenericHttpClient.{ArticleObject, GetArticleById}
import com.softwaremill.sttp._
import domain.entity.Article.ArticleObj
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleError
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import zio.UIO

import scala.concurrent.duration._

final case class GetArticleWithIdClient(
    id: Int,
    currentResult: ArticleObject = UIO.effectTotal(Left(HttpFetchingArticleError()))
)(implicit val config: Config, implicit val httpBackend: SttpBackend[Id, Nothing]) {
  private final case class Implementation(id: Int) extends GetArticleById {
    override def process: ArticleObject =
      for {
        httpRequest <- UIO.effectTotal(
          sttp
            .headers(config.httpClientHeaders)
            .readTimeout(30.second)
            .get(uri"${config.apiEndpoint}/articles/$id")
        )
        response    <- UIO.effectTotal(httpRequest.send())
        doc         <- UIO.effectTotal(parse(response.unsafeBody).getOrElse(Json.Null))
        cursor      <- UIO.effectTotal(doc.hcursor)
        articleJson <- UIO.effectTotal(cursor.downField("article"))
        articleObj <- articleJson
          .as[ArticleObj]
          .fold(_ => UIO.effectTotal(Left(HttpFetchingArticleError())), article => UIO.effectTotal(Right(article)))
      } yield articleObj
  }

  def process = GetArticleWithIdClient(id, Implementation(id).process)
}
