
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import com.github.tototoshi.csv._
import spray.json._
import DefaultJsonProtocol._
import scala.io.StdIn
//
// case class for Person
case class Person(index: Int, firstName: String, lastName: String, email: String)
object CustomerApi extends App {
  // JsonFormat for Person case class
  implicit val personFormat: RootJsonFormat[Person] = jsonFormat4(Person)
  // Load data from CSV file
  val filepath = "C:\\Users\\c22832b\\IdeaProjects\\RestApiAssignment\\src\\main\\scala\\customers_1000.csv"
  val persons: collection.mutable.Buffer[Person] = CSVReader.open(new java.io.File(filepath))
    .toStream
    .drop(1) // Skip the first row containing headers
    .map { case List(index, firstName, lastName, email, _*) =>
      Person(index.toInt, firstName, lastName, email)
    }.toBuffer
  // Akka HTTP setup
  implicit val system: ActorSystem = ActorSystem("Validation")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  // Route for validation, data retrieval, deletion, and update
  val validationRoute: Route =
    pathPrefix("api") {
      concat(
        path("postvalidation") {
          post {
            entity(as[String]) { input =>
              val json = input.parseJson
              val personOpt = json.convertTo[Option[Person]]
              personOpt match {
                case Some(person) =>
                  validateAndSavePerson(person)
                case None =>
                  complete(StatusCodes.BadRequest -> "Invalid JSON format.")
              }
            }
          }
        }~
          path("allData") {
            get {
              complete(StatusCodes.OK -> persons.map(p => s"${p.index},${p.firstName},${p.lastName},${p.email}").mkString("\n"))
            }
          }~
          path("emp"/IntNumber){ index =>
            get{
              persons.find(_.index == index) match {
                case Some(person) =>
                  complete(StatusCodes.OK -> s"${person.index},${person.firstName},${person.lastName},${person.email}")
                case None =>
                  complete(StatusCodes.NotFound -> "Person not found.")
              }
            }~
              delete {
                persons.indexWhere(_.index == index) match {
                  case -1 =>
                    complete(StatusCodes.NotFound -> "Person not found.")
                  case idx =>
                    persons.remove(idx)      // remove data from list
                    complete(StatusCodes.OK -> "Person deleted successfully.")
                }
              } ~
              put {
                entity(as[String]) { input =>
                  val fields = input.split(",")
                  if(fields.length != 3){
                    complete(StatusCodes.BadRequest -> "Invalid input. Expected: First name, Last Name, Email")
                  }
                  else {
                    try {
                      val json = input.parseJson
                      val firstName = json.asJsObject.fields("firstName").convertTo[String]
                      val lastName = json.asJsObject.fields("lastName").convertTo[String]
                      val email = json.asJsObject.fields("email").convertTo[String]

                      val person = Person(index, firstName, lastName, email)
                      updatePerson(index, person)
                    } catch {
                      case _: NumberFormatException =>
                        complete(StatusCodes.BadRequest -> "Index must be an integer.")
                      case _: Throwable =>                               // server errors exception handle
                        complete(StatusCodes.InternalServerError -> "Internal server error.")
                    }
                  }
                }
              }
          }
      )
    }
  // Validation func for person data
  def validateData(person: Person): Option[String] = person match {
    case Person(index, _, _, _) if index < 0 =>
      Some("Index should be a positive number.")
    case Person(_, firstName, _, _) if !firstName.matches("[a-zA-Z]+") =>
      Some("First name should contain only alphabets.")
    case Person(_, _, lastName, _) if !lastName.matches("[a-zA-Z]+") =>
      Some("Last name should contain only alphabets.")
    case Person(_, _, _, email) if !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") =>
      Some("Invalid email format.")
    case _ =>
      None
  }
  // Function to validate, save a person, and return appropriate response
  def validateAndSavePerson(person: Person): Route = {
    validateData(person) match {
      case Some(errorMsg) =>
        complete(StatusCodes.BadRequest -> errorMsg)
      case None =>
        persons += person
        complete(StatusCodes.OK -> "Data is valid.")
    }
  }
  // Function to update a person by index
  def updatePerson(index: Int, updatedPerson: Person): Route = {
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
  }
  // Start the HTTP server
  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(validationRoute, "localhost", 8080)
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
