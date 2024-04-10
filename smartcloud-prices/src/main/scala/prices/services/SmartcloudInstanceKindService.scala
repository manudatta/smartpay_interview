package prices.services

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.circe._

import prices.data._
import org.http4s.client.Client


object SmartcloudInstanceKindService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Concurrent](httpClient: Resource[IO,Client[IO]], config: Config): InstanceKindService[F] = new SmartcloudInstanceKindService(httpClient,config)

  private final class SmartcloudInstanceKindService[F[_]: Concurrent](
      httpClient: Resource[IO,Client[IO]],
      config: Config
  ) extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    val getAllUri = s"${config.baseUri}/instances"

    def getDataFromProxy() : List[String]  = {
      List("sc2-micro", "sc2-small", "sc2-medium") // Dummy data. Your implementation should call the smartcloud API.
    }

    override def getAll(): F[List[InstanceKind]] =
        getDataFromProxy().map(InstanceKind(_))
        .pure[F]

  }

}
