package com.example.sqslib.builders;

import com.example.sqslib.iata.FlightLegIdentifierType;
import com.example.sqslib.iata.FlightLegType;
import com.example.sqslib.iata.IATAAIDXFlightLegNotifRQ;
import com.example.sqslib.iata.IATAAIDXFlightLegRQ;
import com.example.sqslib.iata.IATAAIDXFlightLegRS;
import com.example.sqslib.iata.OperationTimeType;
import com.example.sqslib.iata.OperationalStatusType;
import com.example.sqslib.iata.SuccessType;
import com.example.sqslib.iata.UsageType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author ian.paris
 * @since 2026-01-11
 */
@Component
public class FlightLegBuilder {

	public IATAAIDXFlightLegRS buildFlightLegRs(IATAAIDXFlightLegRQ rq, Map<String, String> metadata) {
		// 1. Instanciar la respuesta raíz
		IATAAIDXFlightLegRS response = new IATAAIDXFlightLegRS();

		// ---------------------------------------------------------
		// A. SETEO DE ATRIBUTOS (HEADERS XML)
		// ---------------------------------------------------------

		// Obligatorio: Versión del esquema (Coincide con tus XSD 21.3)
		response.setVersion(rq.getVersion());

		// Timestamp actual (El Adapter1 se encargará del formato ISO 8601)
		response.setTimeStamp(LocalDateTime.now());

		// Trazabilidad: Es VITAL devolver el mismo ID que recibiste para que (B) sepa qué responder
		response.setCorrelationID(rq.getCorrelationID());
		response.setTransactionIdentifier(rq.getTransactionIdentifier());

		// Identificador único de este mensaje de respuesta
		response.setTransactionStatusCode("Success"); // O "End" según el flujo
		response.setSequenceNmbr(rq.getSequenceNmbr());
		response.setTarget(rq.getTarget());

		// ---------------------------------------------------------
		// B. SETEO DE ESTADO (SUCCESS vs ERRORS)
		// ---------------------------------------------------------

		// En IATA, la presencia del elemento "Success" vacío indica éxito.
		// Si hubiera error, dejarías Success en null y llenarías setErrors(...)
		SuccessType success = new SuccessType();
		response.setSuccess(success);

		// ---------------------------------------------------------
		// C. SETEO DEL PAYLOAD (DATOS DE NEGOCIO)
		// ---------------------------------------------------------

		// Crear la info del vuelo (FlightLeg)
		FlightLegType flightLegType = new FlightLegType();
		FlightLegIdentifierType legId = new FlightLegIdentifierType();

		FlightLegIdentifierType.Airline airline = new FlightLegIdentifierType.Airline();
		airline.setValue(rq.getAirline().getCode());
		airline.setCodeContext(rq.getAirline().getCode());
		legId.setAirline(airline);
		legId.setFlightNumber(metadata.get("flight_number"));

		FlightLegIdentifierType.DepartureAirport departureAirport = new FlightLegIdentifierType.DepartureAirport();
		departureAirport.setValue(metadata.get("departure_airport"));
		departureAirport.setCodeContext(metadata.get("flight_number"));
		legId.setDepartureAirport(departureAirport);

		FlightLegIdentifierType.ArrivalAirport arrivalAirport = new FlightLegIdentifierType.ArrivalAirport();
		arrivalAirport.setValue(metadata.get("arrival_airport"));
		arrivalAirport.setCodeContext(metadata.get("flight_number"));
		legId.setArrivalAirport(arrivalAirport);
		flightLegType.setLegIdentifier(legId);

		// --- B. Datos del Vuelo (LegData) ---
		var legData = new FlightLegType.LegData();

		// B.1 Estado Operativo (Ej. "Schuduled", "OffBlock", "Airborne")
		var opStatus = new OperationalStatusType();
		opStatus.setValue("Schuduled");
		opStatus.setCodeContext("Operational");
		legData.getOperationalStatuses().add(opStatus);

		// B.2 Tiempos (Ej. Scheduled Time of Departure)
		var std = new OperationTimeType();
		std.setTimeType("S"); // S = Scheduled
		std.setOperationQualifier("TD"); // TD = Time of Departure
		std.setValue("+2"); // La hora
		legData.getOperationTimes().add(std);

		// B.3 Recursos de Aeropuerto (Ej. Gate/Terminal)
		var airportRes = new FlightLegType.LegData.AirportResources();
		airportRes.setUsage(UsageType.PLANNED);
		legData.getAirportResources().add(airportRes);

		// Asignar LegData al FlightLeg
		flightLegType.setLegData(legData);

		// Agregarlo a la lista
		response.getFlightLegs().add(flightLegType);

		return response;
	}

	public IATAAIDXFlightLegRS buildFlightLegRsToNotifRq(IATAAIDXFlightLegNotifRQ rq, Map<String, String> metadata) {
		// 1. Instanciar la respuesta raíz
		IATAAIDXFlightLegRS response = new IATAAIDXFlightLegRS();

		// ---------------------------------------------------------
		// A. SETEO DE ATRIBUTOS (HEADERS XML)
		// ---------------------------------------------------------

		// Obligatorio: Versión del esquema (Coincide con tus XSD 21.3)
		response.setVersion(rq.getVersion());

		// Timestamp actual (El Adapter1 se encargará del formato ISO 8601)
		response.setTimeStamp(LocalDateTime.now());

		// Trazabilidad: Es VITAL devolver el mismo ID que recibiste para que (B) sepa qué responder
		response.setCorrelationID(rq.getCorrelationID());
		response.setTransactionIdentifier(rq.getTransactionIdentifier());

		// Identificador único de este mensaje de respuesta
		response.setTransactionStatusCode("Success"); // O "End" según el flujo
		response.setSequenceNmbr(rq.getSequenceNmbr());
		response.setTarget(rq.getTarget());

		// ---------------------------------------------------------
		// B. SETEO DE ESTADO (SUCCESS vs ERRORS)
		// ---------------------------------------------------------

		// En IATA, la presencia del elemento "Success" vacío indica éxito.
		// Si hubiera error, dejarías Success en null y llenarías setErrors(...)
		SuccessType success = new SuccessType();
		response.setSuccess(success);

		// ---------------------------------------------------------
		// C. SETEO DEL PAYLOAD (DATOS DE NEGOCIO)
		// ---------------------------------------------------------

		// Crear la info del vuelo (FlightLeg)
		FlightLegType flightLegType = new FlightLegType();
		FlightLegIdentifierType legId = new FlightLegIdentifierType();

		FlightLegIdentifierType.Airline airline = new FlightLegIdentifierType.Airline();
		airline.setValue(rq.getFlightLegs().getFirst().getLegIdentifier().getAirline().getValue());
		airline.setCodeContext(rq.getFlightLegs().getFirst().getLegIdentifier().getAirline().getCodeContext());
		legId.setAirline(airline);
		legId.setFlightNumber(rq.getFlightLegs().getFirst().getLegIdentifier().getFlightNumber());

		FlightLegIdentifierType.DepartureAirport departureAirport = new FlightLegIdentifierType.DepartureAirport();
		departureAirport.setValue(rq.getFlightLegs().getFirst().getLegIdentifier().getDepartureAirport().getValue());
		departureAirport.setCodeContext(rq.getFlightLegs().getFirst().getLegIdentifier().getDepartureAirport().getCodeContext());
		legId.setDepartureAirport(departureAirport);

		FlightLegIdentifierType.ArrivalAirport arrivalAirport = new FlightLegIdentifierType.ArrivalAirport();
		arrivalAirport.setValue(rq.getFlightLegs().getFirst().getLegIdentifier().getArrivalAirport().getValue());
		arrivalAirport.setCodeContext(rq.getFlightLegs().getFirst().getLegIdentifier().getDepartureAirport().getCodeContext());
		legId.setArrivalAirport(arrivalAirport);
		flightLegType.setLegIdentifier(legId);

		// --- B. Datos del Vuelo (LegData) ---
		var legData = new FlightLegType.LegData();

		// B.1 Estado Operativo (Ej. "Schuduled", "OffBlock", "Airborne")
		var opStatus = new OperationalStatusType();
		opStatus.setValue("Schuduled");
		opStatus.setCodeContext("Operational");
		legData.getOperationalStatuses().add(opStatus);

		// B.2 Tiempos (Ej. Scheduled Time of Departure)
		var std = new OperationTimeType();
		std.setTimeType("S"); // S = Scheduled
		std.setOperationQualifier("TD"); // TD = Time of Departure
		std.setValue("+2"); // La hora
		legData.getOperationTimes().add(std);

		// B.3 Recursos de Aeropuerto (Ej. Gate/Terminal)
		var airportRes = new FlightLegType.LegData.AirportResources();
		airportRes.setUsage(UsageType.PLANNED);
		legData.getAirportResources().add(airportRes);

		// Asignar LegData al FlightLeg
		flightLegType.setLegData(legData);

		// Agregarlo a la lista
		response.getFlightLegs().add(flightLegType);

		return response;
	}
}
