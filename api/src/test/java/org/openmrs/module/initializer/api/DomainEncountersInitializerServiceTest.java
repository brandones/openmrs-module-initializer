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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
		{
			Patient pt = new Patient();
			pt.setUuid("a03e395c-b881-49b7-b6fc-983f6bddc7fc");
			pt.addName(new PersonName("Frodo", null, "Baggins"));
			PatientIdentifier pid = new PatientIdentifier("0001", pit, xanadu);
			pid.setPreferred(true);
			pt.addIdentifier(pid);
			pt.setGender("M");
			pt.setDateCreated(new DateTime(2016, 3, 10, 0, 0, 0).toDate());
			ps.savePatient(pt);
		}
	}
	
	@Test
	public void loadEncounters_shouldLoadAccordingToCsvFiles() {
		
		// Replay
		getService().loadEncounters();
		
		// Verify creation of Peregrin (Pippin), who has all fields, multiple addresses,
		// and multiple names, including whitespace
		{
		}
		// Verify creation of Meriadoc (Mary), who has no optional data
		{
        }

		// Verify edit
		{
		}
		// Verif retire
		{
		}
	}
	
}
