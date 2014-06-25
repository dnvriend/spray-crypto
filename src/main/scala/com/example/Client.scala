package com.example

import akka.actor.ActorSystem
import scala.concurrent.{Await, Future}
import spray.http._
import spray.client.pipelining._
import com.example.Security.{EncryptRequest, EncryptResponse}
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object Client extends App {
  import Security.JsonMarshaller._
  implicit val system = ActorSystem("Client", ConfigFactory.load("client.conf"))
  implicit val executionContext = system.dispatcher

  val pipeline: HttpRequest => Future[EncryptResponse] =
    sendReceive ~> unmarshal[EncryptResponse]

  val start = System.currentTimeMillis()
  val futures = (1 to 1000).toList.map { i =>
    pipeline(Post("http://localhost:8080/crypto/encrypt", EncryptRequest("aes", Option("Hello World!" + i))))
  }
  val future = Future.sequence(futures)
  Await.result(future, 30 seconds)
  val end = System.currentTimeMillis()

  val startRouter = System.currentTimeMillis()
  val futuresRouter = (1 to 1000).toList.map { i =>
    pipeline(Post("http://localhost:8080/crypto/encryptRouter", EncryptRequest("aes", Option("Hello World!" + i))))
  }
  val futureRouter = Future.sequence(futures)
  Await.result(futureRouter, 30 seconds)
  val endRouter = System.currentTimeMillis()

  val startCluster = System.currentTimeMillis()
  val futuresCluster = (1 to 1000).toList.map { i =>
    pipeline(Post("http://localhost:8080/crypto/encryptCluster", EncryptRequest("aes", Option("Hello World!" + i))))
  }
  val futureCluster = Future.sequence(futuresCluster)
  Await.result(futureCluster, 30 seconds)
  val endCluster = System.currentTimeMillis()

  system.shutdown()
  println(
    s"""No router:
       |==========
       |Throughput for 1000 encrypts: ${end - start} ms
       |
       |With router:
       |============
       |Throughput for 1000 encrypts: ${endRouter - startRouter} ms
       |
       |With cluster:
       |============
       |Throughput for 1000 encrypts: ${endCluster - startCluster} ms
     """.stripMargin)
}
