/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer ptated at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.initializer.api;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.DomainBaseModuleContextSensitiveTest;
import org.openmrs.module.initializer.InitializerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

public class DomainProgramEnrollmentsInitializerServiceTest extends DomainBaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("programWorkflowService")
	private ProgramWorkflowService pws;
	
	@Override
	protected String getDomain() {
		return InitializerConstants.DOMAIN_PROGRAM_ENROLLMENTS;
	}
	
	@Before
	public void setup() {
		LocationService ls = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		PatientIdentifierType pit = ps.getPatientIdentifierTypeByName("Old Identification Number");
		Location xanadu = ls.getLocation("Xanadu");
		
		// a patient for the enrollments
		Patient pt = new Patient();
		pt.setUuid("a03e395c-b881-49b7-b6fc-983f6bddc7fc");
		pt.addName(new PersonName("Frodo", null, "Baggins"));
		PatientIdentifier pid = new PatientIdentifier("0001", pit, xanadu);
		pid.setPreferred(true);
		pt.addIdentifier(pid);
		pt.setGender("M");
		pt.setDateCreated(new DateTime(2016, 3, 10, 0, 0, 0).toDate());
		ps.savePatient(pt);
		
		// an enrollment to edit
		{
			PatientProgram pp = new PatientProgram();
			pp.setUuid("f2a345cc-ffbc-4350-aeef-10ba109cf924");
			Program program = pws.getProgramByName("MDR-TB PROGRAM");
			pp.setProgram(program);
			pp.setPatient(pt);
			pp.setDateEnrolled(new Date());
			pws.savePatientProgram(pp);
		}
		
		// an enrollment to void
		{
			PatientProgram pp = new PatientProgram();
			pp.setUuid("9b73d578-3f15-4cea-9415-4d07b8c337ee");
			Program program = pws.getProgramByName("HIV PROGRAM");
			pp.setProgram(program);
			pp.setPatient(pt);
			pp.setDateEnrolled(new Date());
			pws.savePatientProgram(pp);
		}
	}
	
	@Test
	public void loadProgramEnrollments_shouldLoadAccordingToCsvFiles() {
		
		// Replay
		getService().loadProgramEnrollments();
		
		// Verify creation of a program enrollment
		{
			PatientProgram pp = pws.getPatientProgramByUuid("ac5b0ad6-f7e0-4fcc-8c87-98b7dab6a6fe");
			Assert.assertNotNull(pp);
			Date enrollDate = new DateTime(2018, 1, 1, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(enrollDate, pp.getDateEnrolled());
			Assert.assertEquals("a03e395c-b881-49b7-b6fc-983f6bddc7fc", pp.getPatient().getUuid());
			Assert.assertEquals("Xanadu", pp.getLocation().getName());
			Assert.assertEquals("HIV PROGRAM", pp.getProgram().getName());
		}
		// Verify edit
		{
			PatientProgram pp = pws.getPatientProgramByUuid("f2a345cc-ffbc-4350-aeef-10ba109cf924");
			Assert.assertNotNull(pp);
			Date enrollDate = new DateTime(2018, 2, 1, 10, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(enrollDate, pp.getDateEnrolled());
			Date completeDate = new DateTime(2018, 10, 1, 10, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(completeDate, pp.getDateCompleted());
			Assert.assertEquals("a03e395c-b881-49b7-b6fc-983f6bddc7fc", pp.getPatient().getUuid());
			Assert.assertEquals("Xanadu", pp.getLocation().getName());
			Assert.assertEquals("HIV PROGRAM", pp.getProgram().getName());
			Assert.assertEquals("DIED", pp.getOutcome().getName().toString());
		}
		// Verif retire
		{
			PatientProgram pp = pws.getPatientProgramByUuid("9b73d578-3f15-4cea-9415-4d07b8c337ee");
			Assert.assertNotNull(pp);
			Assert.assertTrue(pp.isVoided());
		}
	}
	
}
