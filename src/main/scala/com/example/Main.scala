package com.example

import org.apache.shiro.crypto.{BlowfishCipherService, AesCipherService}
import org.apache.shiro.codec.{Base64, CodecSupport}
import org.apache.shiro.util.ByteSource
import spray.routing.SimpleRoutingApp
import scala.util.{Failure, Success, Try}
import akka.actor._
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Failure
import scala.Some
import scala.util.Success
import scala.util.Failure
import scala.Some
import scala.util.Success

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

trait BlowfishCrypto extends Crypto {
  object Blowfish {
    val passPhrase = "abcdefg"
    val cipher = new BlowfishCipherService
  }

  override def encrypt(plainText: String): Try[String] =
    Try {
      Blowfish.cipher.encrypt(plainText.getBytes, Blowfish.passPhrase.getBytes).toBase64
    }

  override def decrypt(base64Encrypted: String): Try[String] =
    Try {
      val byteSource: ByteSource = ByteSource.Util.bytes(base64Encrypted)
      val decryptedToken = Blowfish.cipher.decrypt(Base64.decode(byteSource.getBytes), Blowfish.passPhrase.getBytes)
      CodecSupport.toString(decryptedToken.getBytes)
    }

}

object Security {
  val crypters = List("aes", "blow")

  case class EncryptRequest(crypto: String, msg: Option[String]) {
    require(crypters.contains(crypto.toLowerCase))
  }

  case class EncryptResponse(crypto: String, encrypted: Option[String])

  case class DecryptRequest(crypto: String, msg: Option[String]) {
    require(crypters.contains(crypto.toLowerCase))
  }

  case class DecryptResponse(crypto: String, decrypted: Option[String])

  object JsonMarshaller extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val encryptRequestFormat = jsonFormat2(Security.EncryptRequest)
    implicit val encryptResponseFormat = jsonFormat2(Security.EncryptResponse)
    implicit val decryptRequestFormat = jsonFormat2(Security.DecryptRequest)
    implicit val decryptResponseFormat = jsonFormat2(Security.DecryptResponse)
  }

  def props = Props(new Security)
}

class Security extends Actor with ActorLogging {

  import Security._

  val aesService = context.actorOf(Props(new Actor with AESCrypto with ActorLogging {
    override def receive: Actor.Receive = {
      case EncryptRequest(_, Some(text)) =>
        encrypt(text) match {
          case Success(encr) =>
            log.info("Encrypting: {}, result: {}", text, encr)
            sender ! EncryptResponse("aes", Some(encr))
          case Failure(cause) =>
            log.error(cause, "Could not encrypt")
            sender ! EncryptResponse("AES", None)
        }
      case DecryptRequest(_, Some(text)) =>
        decrypt(text) match {
          case Success(decr) =>
            log.info("Decrypting: {}, result: {}", text, decr)
            sender ! DecryptResponse("aes", Some(decr))
          case Failure(cause) =>
            log.error(cause, "Could not decrypt")
            sender ! DecryptResponse("aes", None)
        }
    }
  }))

  val blowService = context.actorOf(Props(new Actor with BlowfishCrypto with ActorLogging {
    override def receive: Actor.Receive = {
      case EncryptRequest(_, Some(text)) =>
        encrypt(text) match {
          case Success(encr) =>
            log.info("Encrypting: {}, result: {}", text, encr)
            sender ! EncryptResponse("blow", Some(encr))
          case Failure(cause) =>
            log.error(cause, "Could not encrypt")
            sender ! EncryptResponse("blow", None)
        }
      case DecryptRequest(_, Some(text)) =>
        decrypt(text) match {
          case Success(decr) =>
            log.info("Decrypting: {}, result: {}", text, decr)
            sender ! DecryptResponse("blow", Some(decr))
          case Failure(cause) =>
            log.error(cause, "Could not decrypt")
            sender ! DecryptResponse("blow", None)
        }
    }
  }))

  override def receive: Receive = {
    case msg @ EncryptRequest("aes", Some(text)) => aesService forward msg
    case msg @ EncryptRequest("blow", Some(text)) => blowService forward msg

    case msg @ DecryptRequest("aes", Some(text)) => aesService forward msg
    case msg @ DecryptRequest("blow", Some(text)) => blowService forward msg
  }
}

object Main extends App with SimpleRoutingApp {

  import Security.JsonMarshaller._

  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(15.seconds)

  // we maken onze security actor die AES gaan encrypten en decrypten
  val security = system.actorOf(Security.props, "security")

  // we starten onze spray REST service
  startServer(interface = "localhost", port = 8080) {
    pathPrefix("crypto") {
      path("encrypt") {
        post {
          entity(as[Security.EncryptRequest]) { req =>
            complete {
              (security ? req).mapTo[Security.EncryptResponse]
            }
          }
        }
      } ~
      path("decrypt") {
        post {
          entity(as[Security.DecryptRequest]) { req =>
            complete {
              (security ? req).mapTo[Security.DecryptResponse]
            }
          }
        }
      }
    }
  }
}
