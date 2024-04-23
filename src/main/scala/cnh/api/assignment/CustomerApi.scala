package cnh.api.assignment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.io.StdIn

// case class for Person
case class Person(index: Int, firstName: String, lastName: String, email: String)

object CustomerApi extends App with CustomerRoutes {
  // Akka HTTP setup
  implicit val system: ActorSystem = ActorSystem("Validation")
  implicit val materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Start the HTTP server
  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete {
      case Success(_) =>
        system.terminate()
      case Failure(e) =>
        println(s"Failed to unbind and terminate: ${e.getMessage}")
        system.terminate()
    }
}
