import application.adapters.http_client.sttp_http_client.{
  GetAllArticlesClient,
  GetArticleWithIdClient,
  GetArticleWithKeywordClient
}
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend}
import domain.entity.Config

object NonePureFPRunner extends App {
  implicit val config: Config = Config(
    Map(
      "x-api-key"     -> sys.env("X_API_KEY"),
      "Authorization" -> sys.env("AUTHORIZATION")
    ),
    sys.env("API_ENDPOINT")
  )
  implicit val httpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  println(GetAllArticlesClient().process.currentResult.toString)
  println(GetArticleWithIdClient(4).process.currentResult.toString)
  println(GetArticleWithKeywordClient("Scala").process.currentResult.toString)
}
