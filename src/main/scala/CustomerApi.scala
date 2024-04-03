//import akka.actor.ActorSystem
//import akka.http.scaladsl.Http
//import scala.io.StdIn
//import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
//import akka.stream.ActorMaterializer
//import scala.collection.mutable
//import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.server.Route
//import scala.concurrent.{ExecutionContextExecutor, Future}
//import scala.util.{Failure, Success}
//import com.github.tototoshi.csv._
//import spray.json._
//import DefaultJsonProtocol._
//case class Person(index: Int, firstName: String, lastName: String, email: String)
//object CustomerApi extends App {
//
//
//  //Data validation func
//  def validateData(person: Person): Option[String] = {
//    //to check Person object contains only alphabetic characters
//    if (!person.firstName.matches("[a-zA-Z]+")) {
//      Some("First name should contain only alphabets.")
//    } else if (!person.lastName.matches("[a-zA-Z]+")) {
//      Some("Last name should contain only alphabets.")
//    } else if (!person.email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {  //regular expression pattern to validate the format of the email address in the email field.
//      Some("Invalid email format.")
//    } else {
//      None
//    }
//  }
//  // Load data from CSV file
//  val filepath="C:\\Users\\c22832b\\IdeaProjects\\RestApiAssignment\\src\\main\\scala\\customers_1000.csv"
//  val persons: mutable.Buffer[Person] = CSVReader.open(new java.io.File(filepath))
//    .toStream
//    .drop(1) // Skip the first row containing headers
//    .map { case List(index, firstName, lastName, email, _*) => // Use _* to handle potential extra columns
//      Person(index.toInt, firstName, lastName, email)
//    }.toBuffer
//
//  //Setup for Akka HTTP
//  implicit val system: ActorSystem = ActorSystem("Validation")
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
//  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
//
//  // Route
//  val validationRoute: Route =
//    path("validation") {
//      post {
//        entity(as[String]) { input =>
//          val json = input.parseJson
//          try {
//            val index = json.asJsObject.fields("index").convertTo[Int]
//            val firstName = json.asJsObject.fields("firstName").convertTo[String]
//            val lastName = json.asJsObject.fields("lastName").convertTo[String]
//            val email = json.asJsObject.fields("email").convertTo[String]
//            val person = Person(index, firstName, lastName, email)
//            validateData(person) match {
//              case Some(errorMsg) =>
//                complete(StatusCodes.BadRequest -> errorMsg)
//              case None =>
//                persons += person
//                complete(StatusCodes.OK -> "Data is valid.")
//            }
//          } catch {
//            case _: NumberFormatException =>
//              complete(StatusCodes.BadRequest -> "Please insert a valid user ID.")
//            case _: Throwable =>
//              complete(StatusCodes.InternalServerError -> "Internal server error.")
//          }
//        }
//      }
//    } ~
//      path("data") {
//        get {
//          complete(StatusCodes.OK -> persons.map(p => s"${p.index},${p.firstName},${p.lastName},${p.email}").mkString("\n"))
//        }
//      } ~
//      path("data" / IntNumber) { index =>
//        delete {
//          persons.indexWhere(_.index == index) match {
//            case -1 =>
//              complete(StatusCodes.NotFound -> "Person not found.")
//            case idx =>
//              persons.remove(idx)
//              complete(StatusCodes.OK -> "Person deleted successfully.")
//          }
//        } ~
//          put {
//            entity(as[String]) { input =>
//              val json = input.parseJson
//              try {
//                val firstName = json.asJsObject.fields("firstName").convertTo[String]
//                val lastName = json.asJsObject.fields("lastName").convertTo[String]
//                val email = json.asJsObject.fields("email").convertTo[String]
//                val updatedPerson = Person(index, firstName, lastName, email)
//                validateData(updatedPerson) match {
//                  case Some(errorMsg) =>
//                    complete(StatusCodes.BadRequest -> errorMsg)
//                  case None =>
//                    persons.indexWhere(_.index == index) match {
//                      case -1 =>
//                        complete(StatusCodes.NotFound -> "Person not found.")
//                      case idx =>
//                        persons(idx) = updatedPerson
//                        complete(StatusCodes.OK -> "Person updated successfully.")
//                    }
//                }
//              } catch {
//                case _: Throwable =>
//                  complete(StatusCodes.InternalServerError -> "Internal server error.")
//              }
//            }
//          }
//      }
//  // Start the HTTP server
//  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(validationRoute, "localhost", 8080)
//  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//  StdIn.readLine()
//  // Clean up
//  bindingFuture
//    .flatMap(_.unbind())
//    .onComplete {
//      case Success(_) =>
//        system.terminate()
//      case Failure(e) =>
//        println(s"Failed to unbind and terminate: ${e.getMessage}")
//        system.terminate()
//    }
//}


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import scala.io.StdIn
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import scala.collection.mutable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import com.github.tototoshi.csv._
import spray.json._
import DefaultJsonProtocol._

// Define a case class for Person
case class Person(index: Int, firstName: String, lastName: String, email: String)

object CustomerApi extends App {

  // Validation function for person data
  def validateData(person: Person): Option[String] = {
    val validFirstName = person.firstName.matches("[a-zA-Z]+")
    val validLastName = person.lastName.matches("[a-zA-Z]+")
    val validEmailFormat = person.email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    if (!validFirstName) Some("First name should contain only alphabets.")
    else if (!validLastName) Some("Last name should contain only alphabets.")
    else if (!validEmailFormat) Some("Invalid email format.")
    else None
  }

  // Load data from CSV file
  val filepath = "C:\\Users\\c22832b\\IdeaProjects\\RestApiAssignment\\src\\main\\scala\\customers_1000.csv"
  val persons: mutable.Buffer[Person] = CSVReader.open(new java.io.File(filepath))
    .toStream
    .drop(1) // Skip the first row containing headers
    .map { case List(index, firstName, lastName, email, _*) =>
      Person(index.toInt, firstName, lastName, email)
    }.toBuffer

  // Akka HTTP setup
  implicit val system: ActorSystem = ActorSystem("Validation")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Define the route
  val validationRoute: Route =
    path("validation") {
      post {
        entity(as[String]) { input =>
          val json = input.parseJson
          try {
            val index = json.asJsObject.fields("index").convertTo[Int]
            val firstName = json.asJsObject.fields("firstName").convertTo[String]
            val lastName = json.asJsObject.fields("lastName").convertTo[String]
            val email = json.asJsObject.fields("email").convertTo[String]
            val person = Person(index, firstName, lastName, email)
            validateData(person) match {
              case Some(errorMsg) =>
                complete(StatusCodes.BadRequest -> errorMsg)
              case None =>
                persons += person
                complete(StatusCodes.OK -> "Data is valid.")
            }
          } catch {
            case _: NumberFormatException =>
              complete(StatusCodes.BadRequest -> "Please insert a valid user ID.")
            case _: Throwable =>
              complete(StatusCodes.InternalServerError -> "Internal server error.")
          }
        }
      }
    } ~
      pathPrefix("data") {
        concat(
          get {
            complete(StatusCodes.OK -> persons.map(p => s"${p.index},${p.firstName},${p.lastName},${p.email}").mkString("\n"))
          },
          path(IntNumber) { index =>
            concat(
              delete {
                persons.indexWhere(_.index == index) match {
                  case -1 =>
                    complete(StatusCodes.NotFound -> "Person not found.")
                  case idx =>
                    persons.remove(idx)
                    complete(StatusCodes.OK -> "Person deleted successfully.")
                }
              },
              put {
                entity(as[String]) { input =>
                  val json = input.parseJson
                  try {
                    val firstName = json.asJsObject.fields("firstName").convertTo[String]
                    val lastName = json.asJsObject.fields("lastName").convertTo[String]
                    val email = json.asJsObject.fields("email").convertTo[String]
                    val updatedPerson = Person(index, firstName, lastName, email)
                    validateData(updatedPerson) match {
                      case Some(errorMsg) =>
                        complete(StatusCodes.BadRequest -> errorMsg)
                      case None =>
                        persons.indexWhere(_.index == index) match {
                          case -1 =>
                            complete(StatusCodes.NotFound -> "Person not found.")
                          case idx =>
                            persons(idx) = updatedPerson
                            complete(StatusCodes.OK -> "Person updated successfully.")
                        }
                    }
                  } catch {
                    case _: Throwable =>
                      complete(StatusCodes.InternalServerError -> "Internal server error.")
                  }
                }
              }
            )
          }
        )
      }

  // Start the HTTP server
  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(validationRoute, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  // Clean up
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
