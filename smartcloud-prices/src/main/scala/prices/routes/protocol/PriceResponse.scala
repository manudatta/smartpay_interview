package prices.routes.protocol

import io.circe._
import io.circe.syntax._

import prices.data._


final case class PriceResponse(kind: String, amount: Double)
object PriceResponse {

  implicit val encoder: Encoder[PriceResponse] =
    Encoder.instance[PriceResponse] {
      case PriceResponse(kind, amount) =>
        Json.obj(
          "kind" -> kind.asJson,
          "amount" -> amount.asJson
        )
    }

}

final case class ProxyPriceResponse(kind: String, price: Double, timestamp: String)
