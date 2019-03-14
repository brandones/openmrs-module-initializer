package org.openmrs.module.initializer.api.programenrollments;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class ProgramEnrollmentLineProcessor extends BaseLineProcessor<PatientProgram, ProgramWorkflowService> {
	
	public static final String HEADER_DATE_ENROLLED = "Date Enrolled";
	
	public static final String HEADER_DATE_COMPLETED = "Date Completed";
	
	public static final String HEADER_PERSON_UUID = "Person UUID";
	
	public static final String HEADER_PROGRAM_NAME = "Program Name";
	
	public static final String HEADER_LOCATION = "Location"; // optional
	
	public static final String HEADER_OUTCOME = "Outcome Concept"; // optional
	
	public ProgramEnrollmentLineProcessor(String[] headerLine, ProgramWorkflowService pws) {
		super(headerLine, pws);
	}
	
	@Override
	protected PatientProgram bootstrap(CsvLine line) throws IllegalArgumentException {
		String uuid = getUuid(line.asLine());
		PatientProgram pp = service.getPatientProgramByUuid(uuid);
		
		if (pp == null) {
			pp = new PatientProgram();
			if (!StringUtils.isEmpty(uuid)) {
				pp.setUuid(uuid);
			}
		}
		
		pp.setVoided(getVoidOrRetire(line.asLine()));
		
		return pp;
	}
	
	@Override
	protected PatientProgram fill(PatientProgram pp, CsvLine line) throws IllegalArgumentException {
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		String enrolledDateString = line.get(HEADER_DATE_ENROLLED, true);
		Date enrolledDate = parser.parseDateTime(enrolledDateString).toDate();
		pp.setDateEnrolled(enrolledDate);
		String completedDateString = line.get(HEADER_DATE_COMPLETED);
		if (completedDateString != null && !completedDateString.isEmpty()) {
			Date completedDate = parser.parseDateTime(completedDateString).toDate();
			pp.setDateCompleted(completedDate);
		}
		
		PatientService ps = Context.getPatientService();
		String ptUuid = line.get(HEADER_PERSON_UUID, true);
		Patient pt = ps.getPatientByUuid(ptUuid);
		if (pt == null) {
			throw new IllegalArgumentException("No patient exists with UUID " + ptUuid);
		}
		pp.setPatient(pt);
		
		String programNameString = line.get(HEADER_PROGRAM_NAME, true);
		Program program = service.getProgramByName(programNameString);
		if (program == null) {
			throw new IllegalArgumentException("No program named " + programNameString + " exists");
		}
		pp.setProgram(program);
		
		String locationString = line.getString(HEADER_LOCATION);
		if (locationString != null && !locationString.isEmpty()) {
			LocationService ls = Context.getLocationService();
			Location loc = ls.getLocation(locationString);
			if (loc == null) {
				throw new IllegalArgumentException("No location named " + locationString + " exists");
			}
			pp.setLocation(loc);
		}
		
		String outcomeString = line.get(HEADER_OUTCOME);
		if (outcomeString != null && !outcomeString.isEmpty()) {
			ConceptService cs = Context.getConceptService();
			Concept outcome = lookupConcept(cs, outcomeString);
			if (outcome == null) {
				throw new IllegalArgumentException("Invalid " + HEADER_OUTCOME + ". Should be either a Concept Name "
				        + "in the current preferred locale or a Concept Reference Term like 'CIEL:12345'");
			}
			pp.setOutcome(outcome);
		}
		
		return pp;
	}
	
	private Concept lookupConcept(ConceptService cs, String refTermOrName) {
		if (refTermOrName.contains(":")) {
			String[] termSourceAndTerm = refTermOrName.split(":");
			if (termSourceAndTerm.length != 2) {
				throw new IllegalArgumentException("Concept Reference Terms should be specified like 'Term Source:Code'");
			} else {
				return cs.getConceptByMapping(termSourceAndTerm[1], termSourceAndTerm[0]);
			}
		} else {
			return cs.getConceptByName(refTermOrName);
		}
	}
	
}
