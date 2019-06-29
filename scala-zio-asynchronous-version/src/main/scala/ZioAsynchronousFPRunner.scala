import application.adapters.http_client.sttp_http_client.{
  GetAllArticlesClient,
  GetArticleWithIdClient,
  GetArticleWithKeywordClient
}
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend}
import domain.entity.Config
import zio.DefaultRuntime
import zio.console.putStrLn

object ZioAsynchronousFPRunner extends App {
  implicit val config: Config = Config(
    Map(
      "x-api-key"     -> sys.env("X_API_KEY"),
      "Authorization" -> sys.env("AUTHORIZATION")
    ),
    sys.env("API_ENDPOINT")
  )
  implicit val httpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  val runtime                                        = new DefaultRuntime {}

  runtime.unsafeRun(for {
    program1 <- GetAllArticlesClient().process.currentResult
    _        <- putStrLn(program1.toString())
    program2 <- GetArticleWithIdClient(4).process.currentResult
    _        <- putStrLn(program2.toString)
    program3 <- GetArticleWithKeywordClient("Scala").process.currentResult
    _        <- putStrLn(program3.toString())
  } yield ())
}
