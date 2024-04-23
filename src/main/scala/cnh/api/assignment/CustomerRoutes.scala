package cnh.api.assignment

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}


trait CustomerRoutes extends DataValidation {

  // JsonFormat for Person case class
  implicit val personFormat: RootJsonFormat[Person] = jsonFormat4(Person)

  // Route for validation, data retrieval, deletion, and update
  val routes: Route =
    pathPrefix("customers") {
      concat(

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
        } ~
            get {
              pathEndOrSingleSlash {
              complete(StatusCodes.OK -> persons.map(p => s"${p.index},${p.firstName},${p.lastName},${p.email}").mkString("\n"))
            }
          } ~
          path( IntNumber) { index =>
            get {
              persons.find(_.index == index) match {
                case Some(person) =>
                  complete(StatusCodes.OK -> s"${person.index},${person.firstName},${person.lastName},${person.email}")
                case None =>
                  complete(StatusCodes.NotFound -> "Person not found.")
              }
            } ~
              delete {
                persons.indexWhere(_.index == index) match {
                  case -1 =>
                    complete(StatusCodes.NotFound -> "Person not found.")
                  case idx =>
                    persons.remove(idx) // remove data from list
                    complete(StatusCodes.OK -> "Person deleted successfully.")
                }
              } ~
              put {
                entity(as[String]) { input =>
                  val fields = input.split(",")
                  if (fields.length != 3) {
                    complete(StatusCodes.BadRequest -> "Invalid input. Expected: First name, Last Name, Email")
                  } else {
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
                      case _: Throwable => // server errors exception handle
                        complete(StatusCodes.InternalServerError -> "Internal server error.")
                    }
                  }
                }
              }
          }
      )
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
}
