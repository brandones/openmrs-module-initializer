package org.openmrs.module.initializer.api.encounters;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class EncounterLineProcessor extends BaseLineProcessor<Encounter, EncounterService> {
	
	public static final String HEADER_DATE = "Date";
	
	public static final String HEADER_PT_UUID = "Patient UUID";
	
	public static final String HEADER_LOCATION = "Location";
	
	public static final String HEADER_ENCOUNTER_TYPE = "Encounter Type";
	
	public static final String HEADER_FORM_UUID = "Form UUID"; // optional
	
	public EncounterLineProcessor(String[] headerLine, EncounterService es) {
		super(headerLine, es);
	}
	
	@Override
	protected Encounter bootstrap(CsvLine line) throws IllegalArgumentException {
		String uuid = getUuid(line.asLine());
		Encounter enc = service.getEncounterByUuid(uuid);
		
		if (enc == null) {
			enc = new Encounter();
			if (!StringUtils.isEmpty(uuid)) {
				enc.setUuid(uuid);
			}
		}
		
		enc.setVoided(getVoidOrRetire(line.asLine()));
		
		return enc;
	}
	
	@Override
	protected Encounter fill(Encounter enc, CsvLine line) throws IllegalArgumentException {
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		String dateString = line.get(HEADER_DATE, true);
		Date date = parser.parseDateTime(dateString).toDate();
		enc.setEncounterDatetime(date);
		
		PatientService ps = Context.getPatientService();
		String ptUuid = line.getString(HEADER_PT_UUID);
		Patient pt = ps.getPatientByUuid(ptUuid);
		if (pt == null) {
			throw new IllegalArgumentException("No patient exists with UUID " + ptUuid);
		}
		enc.setPatient(pt);
		
		LocationService ls = Context.getLocationService();
		String locationString = line.getString(HEADER_LOCATION);
		Location loc = ls.getLocation(locationString);
		if (loc == null) {
			throw new IllegalArgumentException("No location named " + locationString + " exists");
		}
		enc.setLocation(loc);
		
		String encounterTypeString = line.getString(HEADER_ENCOUNTER_TYPE);
		EncounterType et = service.getEncounterType(encounterTypeString);
		if (et == null) {
			throw new IllegalArgumentException("No Encounter Type named " + encounterTypeString + " exists");
		}
		enc.setEncounterType(et);
		
		String formUuidString = line.getString(HEADER_FORM_UUID);
		if (formUuidString != null && !formUuidString.isEmpty()) {
			FormService formService = Context.getFormService();
			Form form = formService.getFormByUuid(formUuidString);
			if (form == null) {
				throw new IllegalArgumentException("No Form exists with UUID " + formUuidString);
			}
			enc.setForm(form);
		}
		
		return enc;
	}
	
}
