package prices

import cats.effect._
import cats.syntax.semigroupk._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import prices.config.Config
import prices.services.SmartcloudInstanceKindService
import org.http4s.ember.client.EmberClientBuilder
import prices.routes.PriceRoutes
import prices.services.SmartcloudPriceService
import prices.routes.InstanceKindRoutes

object Server {

  def serve(config: Config): Stream[IO, ExitCode] = {

    val httpClient = EmberClientBuilder.default[IO].build

    val instanceKindService = SmartcloudInstanceKindService.make[IO](
      httpClient,
      SmartcloudInstanceKindService.Config(
        config.smartcloud.baseUri,
        config.smartcloud.token
      )
    )
    val priceService = SmartcloudPriceService.make[IO](
      httpClient,
      SmartcloudPriceService.Config(
        config.smartcloud.baseUri,
        config.smartcloud.token
      )
    )

    val httpApp = (
      PriceRoutes[IO](priceService).routes
        <+> InstanceKindRoutes[IO](instanceKindService).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(Logger.httpApp(true, true)(httpApp))
          .build
          .useForever
      )
  }

}
