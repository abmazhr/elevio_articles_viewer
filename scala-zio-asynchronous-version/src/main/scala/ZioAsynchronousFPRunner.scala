import application.ui.CLIApp
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend}
import domain.entity.Config
import zio.DefaultRuntime

object ZioAsynchronousFPRunner extends App {
  implicit val config: Config = Config(
    Map(
      "x-api-key"     -> sys.env("X_API_KEY"),
      "Authorization" -> sys.env("AUTHORIZATION")
    ),
    sys.env("API_ENDPOINT")
  )
  implicit val httpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  implicit val runtime: DefaultRuntime               = new DefaultRuntime {}

  CLIApp().mainView
}
