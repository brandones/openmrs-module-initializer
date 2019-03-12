package org.openmrs.module.initializer.api.encounters;

import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.initializer.api.CsvParser;

import java.io.IOException;
import java.io.InputStream;

public class ProgramEnrollmentsCsvParser extends CsvParser<Encounter, EncounterService, ProgramEnrollmentLineProcessor> {
	
	public ProgramEnrollmentsCsvParser(InputStream is, EncounterService es) throws IOException {
		super(is, es);
	}
	
	@Override
	protected Encounter save(Encounter instance) {
		return service.saveEncounter(instance);
	}
	
	@Override
	protected boolean isVoidedOrRetired(Encounter instance) {
		return instance.getVoided();
	}
	
	@Override
	protected void setLineProcessors(String version, String[] headerLine) {
		addLineProcessor(new ProgramEnrollmentLineProcessor(headerLine, service));
	}
}
