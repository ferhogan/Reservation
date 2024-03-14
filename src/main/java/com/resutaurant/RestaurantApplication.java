package com.resutaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import io.muserver.rest.Description;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application class for managing restaurant reservations.
 */
@SpringBootApplication
public class RestaurantApplication {

	/** List to store reservations. */
	private static final List<Reservation> reservations = new ArrayList<>();

	/** ObjectMapper for JSON serialization. */
	private static final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	/**
	 * Main method to start the server.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		// Create and configure the server
		MuServer server = MuServerBuilder.httpServer()
				.addHandler(createRestHandler())
				.start();

		// Print server URL upon start
		System.out.println("Server started at " + server.uri());
	}

	/**
	 * Create the REST handler.
	 * @return RestHandlerBuilder instance.
	 */
	public static RestHandlerBuilder createRestHandler() {
		return RestHandlerBuilder.restHandler(new ReservationResource());
	}

	/**
	 * Resource class for managing reservations.
	 */
	@Path("/reservations")
	@Description(value = "Restaurant Reservations", details = "API for managing reservations in a restaurant")
	public static class ReservationResource {

		/**
		 * Method to make a reservation.
		 * @param requestBody JSON request body containing reservation details.
		 * @return JSON response indicating the success or failure of the reservation request.
		 * @throws JsonProcessingException If there is an issue processing JSON.
		 */
		@POST
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@Description("Make a reservation at the restaurant")
		public String makeReservation(String requestBody) throws JsonProcessingException {
			try {
				// Convert JSON request to Reservation object
				Reservation reservation = objectMapper.readValue(requestBody, Reservation.class);

				// Check if reservation is possible
				if (isReservationPossible(reservation)) {
					// Add reservation to the list
					reservations.add(reservation);
					// Return successful response
					return jsonResponse(new Response("Reservation successfully made"));
				} else {
					// Return error message if reservation is not possible
					return jsonResponse(new Response("A reservation already exists for this time period"));
				}
			} catch (IOException e) {
				// Return error message if there is an issue processing the request
				return jsonResponse(new Response("Error processing the request: " + e.getMessage()));
			}
		}

		/**
		 * Method to get all reservations.
		 * @return JSON response containing all reservations.
		 */
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@Description("Get all reservations")
		public String getReservations() {
			// Return all reservations in JSON format
			return jsonResponse(reservations);
		}

		/**
		 * Method to check if a reservation is possible.
		 * @param newReservation The new reservation to check.
		 * @return True if the reservation is possible, false otherwise.
		 */
		private boolean isReservationPossible(Reservation newReservation) {
			LocalDate newReservationDate = newReservation.getDate();
			LocalTime newReservationTime = newReservation.getTime();

			// Check if there is no intersection between dates and times of reservations
			return !reservations.stream().anyMatch(existingReservation ->
					hasIntersection(newReservationDate, newReservationTime, existingReservation));
		}

		/**
		 * Method to check if there is an intersection between dates and times of two reservations.
		 * @param newReservationDate Date of the new reservation.
		 * @param newReservationTime Time of the new reservation.
		 * @param existingReservation Existing reservation to compare with.
		 * @return True if there is an intersection, false otherwise.
		 */
		private boolean hasIntersection(LocalDate newReservationDate, LocalTime newReservationTime, Reservation existingReservation) {
			LocalDate existingReservationDate = existingReservation.getDate();
			LocalTime existingReservationTime = existingReservation.getTime();

			// Check for intersection in reservation time
			return newReservationDate.isEqual(existingReservationDate) && (
					newReservationTime.equals(existingReservationTime) ||
							(newReservationTime.isAfter(existingReservationTime) && newReservationTime.isBefore(existingReservationTime.plusHours(2))) ||
							(newReservationTime.plusHours(2).isAfter(existingReservationTime) && newReservationTime.plusHours(2).isBefore(existingReservationTime.plusHours(2)))
			);
		}

		/**
		 * Method to convert objects to JSON format and return them as response.
		 * @param data The data to convert to JSON.
		 * @return JSON response.
		 */
		private String jsonResponse(Object data) {
			try {
				return objectMapper.writeValueAsString(data);
			} catch (JsonProcessingException e) {
				// Return error message if there is an issue processing the response
				return "{\"message\": \"Error processing the response\"}";
			}
		}
	}
}
