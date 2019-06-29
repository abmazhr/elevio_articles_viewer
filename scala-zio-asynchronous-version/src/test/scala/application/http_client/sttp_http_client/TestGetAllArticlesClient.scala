package application.http_client.sttp_http_client

import application.adapters.http_client.sttp_http_client.GetAllArticlesClient
import com.softwaremill.sttp.Id
import com.softwaremill.sttp.testing.SttpBackendStub
import domain.entity.Article.ArticleRef
import domain.entity.Config
import domain.entity.Error.HttpFetchingAllArticlesError
import org.scalatest.FunSuite
import zio.DefaultRuntime

class TestGetAllArticlesClient extends FunSuite {
  val id          = 1
  val title       = "test!"
  val notes       = "note!"
  val updatedAt   = "26/6/2019"
  val apiEndpoint = "/articles"

  test("calling /articles as valid response to get Right(List[ArticleRef])") {
    implicit val httpBackend: SttpBackendStub[Id, Nothing] =
      SttpBackendStub.synchronous
        .whenRequestMatches(_ => true)
        .thenRespond(
          s""" {"articles": [{"id": $id, "title": "$title", "notes": "$notes", "keywords": [], "updated_at": "$updatedAt"}]} """
        )
    implicit val config: Config = Config(Map.empty, apiEndpoint)
    val runtime                 = new DefaultRuntime {}

    val program = GetAllArticlesClient().process.currentResult
    val actual  = runtime.unsafeRun(program)

    assertResult(Right(List(ArticleRef(id, title, Some(notes), List.empty, updatedAt))))(actual)
  }

  test("calling /articles as invalid response to get Left(HttpFetchingAllArticlesError)") {
    implicit val httpBackend: SttpBackendStub[Id, Nothing] =
      SttpBackendStub.synchronous
        .whenRequestMatches(_ => true)
        .thenRespond(
          s""" {"invalid": ""} """
        )
    implicit val config: Config = Config(Map.empty, apiEndpoint)
    val runtime                 = new DefaultRuntime {}

    val program = GetAllArticlesClient().process.currentResult
    val actual  = runtime.unsafeRun(program)

    assertResult(Left(HttpFetchingAllArticlesError()))(actual)
  }
}
