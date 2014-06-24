package com.example

import org.apache.shiro.crypto.AesCipherService
import org.apache.shiro.codec.{Base64, CodecSupport}
import org.apache.shiro.util.ByteSource
import spray.routing.SimpleRoutingApp
import scala.util.{Failure, Success, Try}
import akka.actor.{ActorSystem, ActorLogging, Actor, Props}
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout

trait Crypto {
  def encrypt(plainText: String): Try[String]
  def decrypt(encrypted: String): Try[String]
}

trait AESCrypto extends Crypto {

  object AES {
    val passPhrase = "j68KkRjq21ykRGAQ"
    val cipher = new AesCipherService
  }

  override def encrypt(plainText: String): Try[String] =
    Try {
      AES.cipher.encrypt(plainText.getBytes, AES.passPhrase.getBytes).toBase64
    }

  override def decrypt(base64Encrypted: String): Try[String] =
    Try {
      val byteSource: ByteSource = ByteSource.Util.bytes(base64Encrypted)
      val decryptedToken = AES.cipher.decrypt(Base64.decode(byteSource.getBytes), AES.passPhrase.getBytes)
      CodecSupport.toString(decryptedToken.getBytes)
    }
}

object Security {

  case class EncryptRequest(crypto: Crypto.Value, request: Option[String])

  case class EncryptResponse(crypto: String, response: Option[String])

  case class DecryptRequest(crypto: Crypto.Value, request: Option[String])

  case class DecryptResponse(crypto: String, response: Option[String])

  object JsonMarshaller extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val encryptResponseFormat = jsonFormat2(Security.EncryptResponse)
    implicit val decryptResponseFormat = jsonFormat2(Security.DecryptResponse)
  }

  // een enumeratie in Scala
  object Crypto extends Enumeration {
    type Crypto = Value
    val AES = Value("AES")
    val UNKNOWN = Value("UNKNOWN")
  }

  def props = Props(new Security)
}

class Security extends Actor with ActorLogging with AESCrypto {

  import Security._

  override def receive: Receive = {
    case EncryptRequest(Crypto.AES, Some(text)) =>
      encrypt(text) match {
        case Success(encr) =>
          log.info("Encrypting: {}, result: {}", text, encr)
          sender ! EncryptResponse(Crypto.AES.toString, Some(encr))
        case Failure(cause) =>
          log.error(cause, "Could not encrypt")
          sender ! EncryptResponse(Crypto.AES.toString, None)
      }

    case DecryptRequest(Crypto.AES, Some(text)) =>
      decrypt(text) match {
        case Success(decr) =>
          log.info("Decrypting: {}, result: {}", text, decr)
          sender ! DecryptResponse(Crypto.AES.toString, Some(decr))
        case Failure(cause) =>
          log.error(cause, "Could not decrypt")
          sender ! DecryptResponse(Crypto.AES.toString, None)
      }
  }
}

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(15.seconds)

  // we maken onze security actor die AES gaan encrypten en decrypten
  val security = system.actorOf(Security.props, "security")

  // we starten onze spray REST service
  startServer(interface = "localhost", port = 8080) {
    import Security.JsonMarshaller._
    pathPrefix("crypto") {
      pathPrefix("aes") {
        path("encrypt") {
          post {
            entity(as[String]) { text =>
              complete {
                (security ? Security.EncryptRequest(Security.Crypto.AES, Option(text))).mapTo[Security.EncryptResponse]
              }
            }
          }
        } ~
        path("decrypt") {
          post {
            entity(as[String]) { encrypted =>
              complete {
                (security ? Security.DecryptRequest(Security.Crypto.AES, Option(encrypted))).mapTo[Security.DecryptResponse]
              }
            }
          }
        }
      }
    }
  }
}
