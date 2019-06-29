package application.http_client.sttp_http_client

import application.adapters.http_client.sttp_http_client.GetArticleWithKeywordClient
import com.softwaremill.sttp.Id
import com.softwaremill.sttp.testing.SttpBackendStub
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleSearchResultError
import org.scalatest.FunSuite
import zio.DefaultRuntime

class TestGetArticleByKeywordClient extends FunSuite {
  val id          = 1
  val keyword     = "Scala"
  val apiEndpoint = s"/articles/$id"

  test(s"calling /articles/$id as valid response to get Right(ArticleObj)") {
    implicit val httpBackend: SttpBackendStub[Id, Nothing] =
      SttpBackendStub.synchronous
        .whenRequestMatches(_ => true)
        .thenRespond(s""" {"results": [{"id": $id}]} """)
    implicit val config: Config = Config(Map.empty, apiEndpoint)
    val runtime                 = new DefaultRuntime {}

    val program = GetArticleWithKeywordClient(keyword).process.currentResult
    val actual  = runtime.unsafeRun(program)
    actual match {
      case Right(article) => assertResult(id)(article.head.id)
      case Left(_)        => fail("This should never happen in this test")
    }
  }

  test(s"calling /articles/$id as invalid response to get Left(HttpFetchingArticleError)") {
    implicit val httpBackend: SttpBackendStub[Id, Nothing] =
      SttpBackendStub.synchronous
        .whenRequestMatches(_ => true)
        .thenRespond(
          s""" {"invalid": "invalid"} """
        )
    implicit val config: Config = Config(Map.empty, apiEndpoint)
    val runtime                 = new DefaultRuntime {}

    val program = GetArticleWithKeywordClient(keyword).process.currentResult
    val actual  = runtime.unsafeRun(program)
    assertResult(Left(HttpFetchingArticleSearchResultError()))(actual)
  }
}
