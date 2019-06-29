package application.http_client.sttp_http_client

import application.adapters.http_client.sttp_http_client.GetArticleWithIdClient
import com.softwaremill.sttp.Id
import com.softwaremill.sttp.testing.SttpBackendStub
import domain.entity.Config
import domain.entity.Error.HttpFetchingArticleError
import org.scalatest.FunSuite

class TestGetArticleByIdClient extends FunSuite {
  val id          = 1
  val name        = "test"
  val email       = "test@test.test"
  val createdAt   = "9/7/2019"
  val title       = "test"
  val summary     = "test"
  val body        = "test"
  val apiEndpoint = s"/articles/$id"

  test(s"calling /articles/$id as valid response to get Right(ArticleObj)") {
    implicit val httpBackend: SttpBackendStub[Id, Nothing] =
      SttpBackendStub.synchronous
        .whenRequestMatches(_ => true)
        .thenRespond(
          s""" {"article": {"id": $id, "author": {"id": $id, "name": "$name", "email": "$email"},"created_at": "$createdAt","title": "$title","translations": [{"summary": "$summary", "body": "$body"}]}} """
        )
    implicit val config: Config = Config(Map.empty, apiEndpoint)

    val actual = GetArticleWithIdClient(id).process.currentResult
    actual match {
      case Right(article) =>
        assertResult(id)(article.id)
        assertResult(name)(article.author.name)
        assertResult(email)(article.author.email)
        assertResult(createdAt)(article.created_at)
        assertResult(title)(article.title)
        assertResult(Some(summary))(article.translations.head.summary)
        assertResult(body)(article.translations.head.body)
      case Left(_) => fail("This should never happen in this test")
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

    val actual = GetArticleWithIdClient(id).process.currentResult
    assertResult(Left(HttpFetchingArticleError()))(actual)
  }
}
