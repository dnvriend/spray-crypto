/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import org.apache.shiro.codec.{ Base64, CodecSupport }
import org.apache.shiro.crypto.AesCipherService
import org.apache.shiro.util.ByteSource
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.SimpleRoutingApp

import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

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
    case EncryptRequest(Crypto.AES, Some(text)) ⇒
      encrypt(text) match {
        case Success(encr) ⇒
          log.info("Encrypting: {}, result: {}", text, encr)
          sender ! EncryptResponse(Crypto.AES.toString, Some(encr))
        case Failure(cause) ⇒
          log.error(cause, "Could not encrypt")
          sender ! EncryptResponse(Crypto.AES.toString, None)
      }

    case DecryptRequest(Crypto.AES, Some(text)) ⇒
      decrypt(text) match {
        case Success(decr) ⇒
          log.info("Decrypting: {}, result: {}", text, decr)
          sender ! DecryptResponse(Crypto.AES.toString, Some(decr))
        case Failure(cause) ⇒
          log.error(cause, "Could not decrypt")
          sender ! DecryptResponse(Crypto.AES.toString, None)
      }
  }
}

trait JsonMarshaller extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val encryptResponseFormat = jsonFormat2(Security.EncryptResponse)
  implicit val decryptResponseFormat = jsonFormat2(Security.DecryptResponse)
}

object Main extends App with SimpleRoutingApp with JsonMarshaller {

  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(15.seconds)

  // create the security actor service that will encrypt/decrypt
  val security = system.actorOf(Security.props, "security")

  // we starten onze spray REST service
  startServer(interface = "localhost", port = 8080) {
    pathPrefix("crypto") {
      pathPrefix("aes") {
        path("encrypt") {
          pathEnd {
            post {
              entity(as[String]) { text ⇒
                complete {
                  (security ? Security.EncryptRequest(Security.Crypto.AES, Option(text))).mapTo[Security.EncryptResponse]
                }
              }
            }
          }
        } ~
          path("decrypt") {
            pathEnd {
              post {
                entity(as[String]) { encrypted ⇒
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
}
