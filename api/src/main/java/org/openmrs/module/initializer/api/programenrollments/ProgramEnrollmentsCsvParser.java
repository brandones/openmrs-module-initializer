package org.openmrs.module.initializer.api.programenrollments;

import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.initializer.api.CsvParser;
import org.openmrs.module.initializer.api.programenrollments.ProgramEnrollmentLineProcessor;

import java.io.IOException;
import java.io.InputStream;

public class ProgramEnrollmentsCsvParser extends CsvParser<PatientProgram, ProgramWorkflowService, ProgramEnrollmentLineProcessor> {
	
	public ProgramEnrollmentsCsvParser(InputStream is, ProgramWorkflowService pws) throws IOException {
		super(is, pws);
	}
	
	@Override
	protected PatientProgram save(PatientProgram instance) {
		return service.savePatientProgram(instance);
	}
	
	@Override
	protected boolean isVoidedOrRetired(PatientProgram instance) {
		return instance.getVoided();
	}
	
	@Override
	protected void setLineProcessors(String version, String[] headerLine) {
		addLineProcessor(new ProgramEnrollmentLineProcessor(headerLine, service));
	}
}
