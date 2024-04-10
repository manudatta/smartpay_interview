package prices.services

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.circe._

import prices.data._
import org.http4s.client.Client
import prices.services.helpers.ProxyRequest
import prices.services.InstanceKindService.Exception.APICallFailure

object SmartcloudInstanceKindService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Concurrent](httpClient: Resource[F, Client[F]], config: Config): InstanceKindService[F] = new SmartcloudInstanceKindService(httpClient, config)

  private final class SmartcloudInstanceKindService[F[_]: Concurrent](
      httpClient: Resource[F, Client[F]],
      config: Config
  ) extends InstanceKindService[F] {


    val getAllUri = s"${config.baseUri}/instances"

    def proxy(): F[List[InstanceKind]] =
      httpClient.use { client =>
        val request = ProxyRequest(config.token)
        Uri.fromString(getAllUri).toOption match {
          case Some(finalUri) => client.run(request.withUri(finalUri)).use(getAndHandleResponse)
          case _              => APICallFailure("Unexpected Error parsing smartcloud container URI").raiseError[F, List[InstanceKind]]
        }
      }

    private def createMsg(status: Status) = s"Failed status_code=${status.code}  message=${Option(status.reason).getOrElse("unknown reason")}"

    private def getAndHandleResponse(response: Response[F]): F[List[InstanceKind]] =
      response.status match {
        case Status.Ok =>
          response.asJsonDecode[List[String]].flatMap(x => x.map(y => InstanceKind(y)).pure[F])
        case status_other =>  println("I was here")
          APICallFailure(createMsg(status_other)).raiseError[F, List[InstanceKind]]
      }

    override def getAll(): F[List[InstanceKind]] =
      proxy()
  }

}
