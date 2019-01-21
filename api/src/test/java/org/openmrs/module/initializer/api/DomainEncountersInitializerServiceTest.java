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
import org.openmrs.PersonName;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.DomainBaseModuleContextSensitiveTest;
import org.openmrs.module.initializer.InitializerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;

public class DomainEncountersInitializerServiceTest extends DomainBaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	@Override
	protected String getDomain() {
		return InitializerConstants.DOMAIN_P;
	}
	
	@Before
	public void setup() {
		LocationService ls = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		PatientIdentifierType pit = ps.getPatientIdentifierTypeByName("Old Identification Number");
		Location xanadu = ls.getLocation("Xanadu");
		
		// a patient for the encounters
        Patient pt = new Patient();
        pt.setUuid("a03e395c-b881-49b7-b6fc-983f6bddc7fc");
        pt.addName(new PersonName("Frodo", null, "Baggins"));
        PatientIdentifier pid = new PatientIdentifier("0001", pit, xanadu);
        pid.setPreferred(true);
        pt.addIdentifier(pid);
        pt.setGender("M");
        pt.setDateCreated(new DateTime(2016, 3, 10, 0, 0, 0).toDate());
        ps.savePatient(pt);

		// an encounter to edit
		{
			Encounter enc = new Encounter();
			enc.setUuid("c64730db-ab24-4036-a2aa-46177e743db9");
			enc.setEncounterDatetime(new DateTime(2018, 1, 1, 0, 0,0, DateTimeZone.UTC).toDate());
			enc.setLocation(xanadu);
			enc.setEncounterType(es.getEncounterType("Scheduled"));
			enc.setPatient(pt);
			es.saveEncounter(enc);
		}

		// an encounter to void
		{
			Encounter enc = new Encounter();
			enc.setUuid("a3f81565-b0d9-44b5-ba5f-665ed8dfd697");
			enc.setEncounterDatetime(new DateTime(2018, 1, 2, 0, 0,0, DateTimeZone.UTC).toDate());
			enc.setLocation(xanadu);
			enc.setEncounterType(es.getEncounterType("Scheduled"));
			enc.setPatient(pt);
			es.saveEncounter(enc);
		}
	}
	
	@Test
	public void loadEncounters_shouldLoadAccordingToCsvFiles() {
		
		// Replay
		getService().loadEncounters();
		
		// Verify creation of an encounter for Frodo
		{
		    Encounter enc = es.getEncounterByUuid("b330f4de-ed7d-4e9d-9ff3-81997e729aa8");
		    Assert.assertNotNull(enc);
		    Date dt = new DateTime(2018, 2, 1, 10, 0, 0, DateTimeZone.UTC).toDate();
		    Assert.assertEquals(dt, enc.getEncounterDatetime());
		    Assert.assertEquals("a03e395c-b881-49b7-b6fc-983f6bddc7fc", enc.getPatient().getUuid());
		    Assert.assertEquals("Xanadu", enc.getLocation().getName());
		    Assert.assertEquals("Scheduled", enc.getEncounterType().getName());
		}
		// Verify edit
		{
		    Encounter enc = es.getEncounterByUuid("c64730db-ab24-4036-a2aa-46177e743db9");
			Assert.assertNotNull(enc);
			Date dt = new DateTime(2018, 1, 1, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(dt, enc.getEncounterDatetime());
		}
		// Verif retire
		{
			Encounter enc = es.getEncounterByUuid("a3f81565-b0d9-44b5-ba5f-665ed8dfd697");
			Assert.assertNotNull(enc);
			Assert.assertTrue(enc.isVoided());
		}
	}
	
}
