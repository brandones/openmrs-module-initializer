package org.openmrs.module.initializer.api.obs;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;

import java.util.Date;
import java.util.UUID;

public class ObsLineProcessor extends BaseLineProcessor<Obs, ObsService> {
	
	public static final String HEADER_DATE = "Date";
	
	public static final String HEADER_PERSON_UUID = "Person UUID";
	
	public static final String HEADER_LOCATION = "Location";
	
	public static final String HEADER_ENCOUNTER_UUID = "Encounter UUID";
	
	public static final String HEADER_CONCEPT = "Concept";
	
	public static final String HEADER_VALUE = "Value";
	
	public static final String HEADER_SET_MEMBERS = "Set Members";
	
	public static final String HEADER_SET_MEMBER_VALUES = "Set Member Values";
	
	public ObsLineProcessor(String[] headerLine, ObsService es) {
		super(headerLine, es);
	}
	
	@Override
	protected Obs bootstrap(CsvLine line) throws IllegalArgumentException {
		String uuid = getUuid(line.asLine());
		Obs obs = service.getObsByUuid(uuid);
		
		if (obs == null) {
			obs = new Obs();
			if (!StringUtils.isEmpty(uuid)) {
				obs.setUuid(uuid);
			}
		}
		
		obs.setVoided(getVoidOrRetire(line.asLine()));
		
		return obs;
	}
	
	@Override
	protected Obs fill(Obs obs, CsvLine line) throws IllegalArgumentException {
		EncounterService es = Context.getEncounterService();
		String encounterUuid = line.get(HEADER_ENCOUNTER_UUID);
		Encounter enc = null;
		if (encounterUuid != null && !encounterUuid.isEmpty()) {
			enc = es.getEncounterByUuid(encounterUuid);
			if (enc == null) {
				throw new IllegalArgumentException("Invalid " + HEADER_ENCOUNTER_UUID);
			} else {
				obs.setEncounter(enc);
			}
		}
		
		Date date = getAndParseDate(line);
		if (date == null) {
			date = dateFromEncounter(enc);
		}
		if (date == null) {
			throwBadHeader(HEADER_DATE);
		}
		obs.setObsDatetime(date);
		
		Person person = getPerson(line);
		if (person == null) {
			person = personFromEncounter(enc);
		}
		if (person == null) {
			throwBadHeader(HEADER_PERSON_UUID);
		}
		obs.setPerson(person);
		
		Location loc = getLocation(line);
		if (loc == null) {
			loc = locationFromEncounter(enc);
		}
		if (loc == null) {
			throwBadHeader(HEADER_LOCATION);
		}
		obs.setLocation(loc);
		
		ConceptService cs = Context.getConceptService();
		Concept c = lookupConcept(cs, line.get(HEADER_CONCEPT, true));
		if (c == null) {
			throw new IllegalArgumentException("Invalid " + HEADER_CONCEPT + ". Should be either a Concept Name "
			        + "in the current preferred locale or a Concept Reference Term like 'CIEL:12345'");
		}
		obs.setConcept(c);
		
		if (c.isSet()) {
			String memberConceptsStr = line.get(HEADER_SET_MEMBERS, true);
			String[] memberConcepts = memberConceptsStr.split(LIST_SEPARATOR);
			String valuesStr = line.get(HEADER_SET_MEMBER_VALUES, true);
			String[] values = valuesStr.split(LIST_SEPARATOR);
			if (memberConcepts.length != values.length) {
				throw new IllegalArgumentException(HEADER_SET_MEMBERS + " and " + HEADER_SET_MEMBER_VALUES + " must "
				        + "have the same number of semicolon-delimited items.");
			}
			for (int i = 0; i < memberConcepts.length; i++) {
				String uuid = computeMemberObsUuid(obs, memberConcepts[i]);
				Obs memberObs = service.getObsByUuid(uuid);
				if (memberObs == null) {
					memberObs = new Obs();
					memberObs.setUuid(uuid);
				}
				memberObs.setEncounter(enc);
				memberObs.setObsDatetime(date);
				memberObs.setPerson(person);
				memberObs.setLocation(loc);
				Concept mc = lookupConcept(cs, memberConcepts[i]);
				memberObs.setConcept(mc);
				setObsValue(memberObs, values[i]);
				obs.addGroupMember(memberObs);
			}
		} else {
			setObsValue(obs, line.get(HEADER_VALUE));
		}
		
		return obs;
	}
	
	private void throwBadHeader(String headerName) throws IllegalArgumentException {
		throw new IllegalArgumentException("Either " + headerName + " or " + HEADER_ENCOUNTER_UUID + " are required.");
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
	
	private Date getAndParseDate(CsvLine line) throws IllegalArgumentException {
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
		String dateString = line.get(HEADER_DATE);
		if (dateString != null && !dateString.isEmpty()) {
			return parser.parseDateTime(dateString).toDate();
		}
		return null;
	}
	
	private Date dateFromEncounter(Encounter enc) {
		if (enc != null) {
			return enc.getEncounterDatetime();
		}
		return null;
	}
	
	private Person getPerson(CsvLine line) throws IllegalArgumentException {
		String personUuidString = line.get(HEADER_PERSON_UUID);
		if (personUuidString != null && !personUuidString.isEmpty()) {
			PersonService ps = Context.getPersonService();
			Person person = ps.getPersonByUuid(personUuidString);
			if (person == null) {
				throw new IllegalArgumentException("Invalid patient from " + HEADER_PERSON_UUID);
			}
			return person;
		} else {
			return null;
		}
	}
	
	private Person personFromEncounter(Encounter enc) {
		if (enc != null) {
			return enc.getPatient();
		}
		return null;
	}
	
	private Location getLocation(CsvLine line) {
		String locString = line.get(HEADER_LOCATION);
		if (locString != null && !locString.isEmpty()) {
			LocationService ls = Context.getLocationService();
			Location loc = ls.getLocation(locString);
			if (loc == null) {
				throw new IllegalArgumentException("Invalid location from " + HEADER_LOCATION);
			}
			return loc;
		}
		return null;
	}
	
	private Location locationFromEncounter(Encounter enc) {
		if (enc != null) {
			return enc.getLocation();
		}
		return null;
	}
	
	private String computeMemberObsUuid(Obs setObs, String memberConceptStr) {
		String hashString = setObs.getUuid() + memberConceptStr;
		return UUID.nameUUIDFromBytes(hashString.getBytes()).toString();
	}
	
	private void setObsValue(Obs obs, String valueStr) {
		Concept c = obs.getConcept();
		ConceptDatatype datatype = c.getDatatype();
		if (datatype.isBoolean()) {
			Boolean value = BooleanUtils.toBoolean(valueStr);
			obs.setValueBoolean(value);
		} else if (datatype.isCoded()) {
			ConceptService cs = Context.getConceptService();
			Concept answer = lookupConcept(cs, valueStr);
			obs.setValueCoded(answer);
		} else if (datatype.isDate() || datatype.isDateTime() || datatype.isTime()) {
			DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
			Date value = parser.parseDateTime(valueStr).toDate();
			obs.setValueDatetime(value);
		} else if (datatype.isNumeric()) {
			Double value = Double.parseDouble(valueStr);
			obs.setValueNumeric(value);
		} else if (datatype.isText()) {
			obs.setValueText(valueStr);
		} // do nothing if datatype.isAnswerOnly(), or is NA
		  // TODO: Handle datatypes Rule, Structured Numeric, and Complex
		  // (ConceptDatatypes.java: https://git.io/fh7Zt)
	}
	
}
