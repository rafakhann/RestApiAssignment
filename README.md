The server will start running at http://localhost:8080.

You can use tools like cURL or Postman to interact with the API endpoints:


1.Validate Person Data Endpoint

 -Method: POST
 
 -Path: /customers/validate
 -Description: Validates and saves the provided person data.
 -Request Body: JSON representation of a person.
 -Response:
  -200 OK: Data is valid.
  -400 Bad Request: Invalid JSON format or validation failed.
List All Persons Endpoint
 -Method: GET
 -Path: /customers/list
 -Description: Retrieves a list of all persons.
 -Response:
  -200 OK: List of persons in CSV format.
  -404 Not Found: No persons found.
Get Person by Index Endpoint
 -Method: GET
 -Path: /customers/employee/{index}
 -Description: Retrieves a person by their index.
 -Path Parameter: index (integer) - The index of the person to retrieve.
 -Response:
  -200 OK: Details of the requested person.
  -404 Not Found: Person not found.
Delete Person by Index Endpoint
 -Method: DELETE
 -Path: /customers/employee/{index}
 -Description: Deletes a person by their index.
 -Path Parameter: index (integer) - The index of the person to delete.
 -Response:
  -200 OK: Person deleted successfully.
  -404 Not Found: Person not found.
Update Person by Index Endpoint
 -Method: PUT
 -Path: /customers/employee/{index}
 -Description: Updates a person by their index.
 -Path Parameter: index (integer) - The index of the person to update.
 -Request Body: JSON representation of the updated person.
 -Response:
  -200 OK: Person updated successfully.
  -400 Bad Request: Invalid input format or validation failed.
  -404 Not Found: Person not found.
