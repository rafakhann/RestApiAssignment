package cnh.api.assignment

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.github.tototoshi.csv.CSVReader

trait DataValidation {
//

  // Load data from CSV file
  val filepath = "C:\\Users\\c22832b\\IdeaProjects\\RestApiAssignment\\src\\main\\scala\\cnh\\api\\assignment\\customers_1000.csv"

  val persons: collection.mutable.Buffer[Person] = CSVReader.open(new java.io.File(filepath))
    .toStream
    .drop(1) // Skip the first row containing headers
    .map { case List(index, firstName, lastName, email, _*) =>
      Person(index.toInt, firstName, lastName, email)
    }.toBuffer

//  val persons: Vector[Person] = CSVReader.open(new java.io.File(filepath))
//    .toStream
//    .map {
//      case List(index, firstName, lastName, email) => Person(index.toInt, firstName, lastName, email)
//    }.toVector



  // Validation func for person data
  def validateData(person: Person): Option[String] = {
    val s1 :String= "[a-zA-Z]+"
    val s2 :String= "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    person match {
    case Person(index, _, _, _) if index < 0 =>
      Some("Index should be a positive number.")
    case Person(_, firstName, _, _) if !firstName.matches(s1) =>
      Some("First name should contain only alphabets.")
    case Person(_, _, lastName, _) if !lastName.matches(s1) =>
      Some("Last name should contain only alphabets.")
    case Person(_, _, _, email) if !email.matches(s2) =>
      Some("Invalid email format.")
    case _ =>
      None
  }
  }

  /// Function to validate, save a person, and return appropriate response
  def validateAndSavePerson(person: Person): Route = {
    validateData(person) match {
      case Some(errorMsg) =>
        complete(StatusCodes.BadRequest -> errorMsg)
      case None =>
        persons += person
        complete(StatusCodes.OK -> "Success.")
    }
  }
}

