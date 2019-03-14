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
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.DomainBaseModuleContextSensitiveTest;
import org.openmrs.module.initializer.InitializerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;

public class DomainObsInitializerServiceTest extends DomainBaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("obsService")
	private ObsService os;
	
	@Override
	protected String getDomain() {
		return InitializerConstants.DOMAIN_OBS;
	}
	
	@Before
	public void setup() {
		LocationService ls = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		ConceptService cs = Context.getConceptService();
		
		Patient pt = ps.getPatient(2);
		Location xanadu = ls.getLocation("Xanadu");
		Concept booleanConcept = cs.getConcept(18);
		
		// an obs to edit
		// NB: obs are special because obsService doesn't update existing obs rows with
		// new data.
		// Rather, it voids the old row and creates a new row. The new row has a new
		// UUID. Thus,
		// while we "edit" the obs by referring to it by UUID in the CSV file, we have
		// to look
		// it up later by other means.
		{
			Date dt = new DateTime(2018, 3, 1, 12, 0, 0, DateTimeZone.UTC).toDate();
			Obs o = new Obs(pt, booleanConcept, dt, xanadu);
			o.setUuid("afc94d43-3aa8-4724-809f-4934b1df3c7b");
			o.setValueBoolean(true);
			os.saveObs(o, "Obs to edit");
		}
		
		// an obs to void
		{
			Date dt = new DateTime(2018, 4, 1, 12, 0, 0, DateTimeZone.UTC).toDate();
			Obs o = new Obs(pt, booleanConcept, dt, xanadu);
			o.setUuid("c87f104b-87c6-4b1d-a5fb-a951adad25c0");
			o.setValueBoolean(false);
			os.saveObs(o, "Obs to void");
		}
	}
	
	@Test
	public void loadObs_shouldLoadAccordingToCsvFiles() {
		
		// Replay
		getService().loadObservations();
		
		LocationService ls = Context.getLocationService();
		PatientService ps = Context.getPatientService();
		EncounterService es = Context.getEncounterService();
		ConceptService cs = Context.getConceptService();
		
		Location xanadu = ls.getLocation("Xanadu");
		
		// Verify creation of a numeric obs, from concept ref term, with encounter
		{
			Obs o = os.getObsByUuid("5a1634a5-ef99-48e3-919e-fcf2e6464ee3");
			Assert.assertNotNull(o);
			Date dt = new DateTime(2019, 1, 4, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(dt, o.getObsDatetime());
			Assert.assertEquals(ps.getPatient(7), o.getPerson());
			Assert.assertEquals(xanadu, o.getLocation());
			Assert.assertEquals(es.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac"), o.getEncounter());
			Assert.assertEquals((Integer) 5089, o.getConcept().getId());
			Assert.assertEquals((Double) 90.0, o.getValueNumeric());
		}
		// Verify creation of a boolean obs, from concept name
		{
			Obs o = os.getObsByUuid("ed88d046-a558-4112-a2fd-275ee4da4f66");
			Assert.assertNotNull(o);
			Date dt = new DateTime(2019, 1, 2, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(dt, o.getObsDatetime());
			Assert.assertEquals(ps.getPatient(7), o.getPerson());
			Assert.assertEquals(xanadu, o.getLocation());
			Assert.assertEquals((Integer) 18, o.getConcept().getId());
			Assert.assertEquals(false, o.getValueBoolean());
		}
		// Verify creation of a coded obs, person, location, and date inherited from
		// encounter
		{
			Obs o = os.getObsByUuid("9fd15da4-27e6-43c7-a3cf-c718997926f4");
			Assert.assertNotNull(o);
			Assert.assertEquals(es.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac"), o.getEncounter());
			Date dt = new DateTime(2008, 8, 1, 0, 0, 0).toDate(); // standardTestDataset times are local time
			Assert.assertEquals(dt, new Date(o.getObsDatetime().getTime()));
			Assert.assertEquals(ps.getPatient(7), o.getPerson());
			Assert.assertEquals(ls.getLocation("Unknown Location"), o.getLocation());
			Assert.assertEquals((Integer) 4, o.getConcept().getId());
			Assert.assertNotNull(o.getValueCoded());
			Assert.assertEquals((Integer) 6, o.getValueCoded().getId());
		}
		// Verify creation of a text obs
		{
			Obs o = os.getObsByUuid("734a37ea-cbb1-499c-ad01-5a07b752ab52");
			Assert.assertNotNull(o);
			Date dt = new DateTime(2019, 1, 2, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(dt, o.getObsDatetime());
			Assert.assertEquals(ps.getPatient(7), o.getPerson());
			Assert.assertEquals(xanadu, o.getLocation());
			Assert.assertEquals((Integer) 19, o.getConcept().getId());
			Assert.assertEquals("Slim Jims", o.getValueText());
		}
		// Verify creation of a datetime obs
		{
			Obs o = os.getObsByUuid("d346cd50-1506-4677-8f00-258cbbaa8d4f");
			Assert.assertNotNull(o);
			Date dt = new DateTime(2019, 1, 2, 13, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(dt, o.getObsDatetime());
			Assert.assertEquals(ps.getPatient(7), o.getPerson());
			Assert.assertEquals(xanadu, o.getLocation());
			Assert.assertEquals((Integer) 20, o.getConcept().getId());
			Date expectedValue = new DateTime(2018, 7, 1, 0, 0, 0, DateTimeZone.UTC).toDate();
			Assert.assertEquals(expectedValue, o.getValueDatetime());
		}
		// Verify creation of an obs set
		{
			Obs o = os.getObsByUuid("fd93d046-a558-4112-a2fd-275ee4da4f66");
			Assert.assertNotNull(o);
			Assert.assertEquals((Integer) 23, o.getConcept().getId());
			Assert.assertThat(o.isObsGrouping(), is(true));
			Set<Obs> members = o.getGroupMembers();
			Assert.assertEquals(2, members.size());
			boolean foundFoodAssistance = false;
			boolean foundFavoriteFood = false;
			for (Obs mo : members) {
				if (mo.getConcept().isNamed("FOOD ASSISTANCE")) {
					foundFoodAssistance = true;
					Assert.assertEquals(true, mo.getValueBoolean());
				} else if (mo.getConcept().isNamed("FAVORITE FOOD, NON-CODED")) {
					foundFavoriteFood = true;
					Assert.assertEquals("Blood of enemies", mo.getValueText());
				} else {
					Assert.fail("Observation has unexpected member " + mo.getConcept().getName());
				}
			}
			if (!foundFoodAssistance) {
				Assert.fail("Didn't find expected member observation 'FOOD ASSISTANCE'");
			}
			if (!foundFavoriteFood) {
				Assert.fail("Didn't find expected member observation 'FAVORITE FOOD, NON-CODED'");
			}
		}
		// Verify edit
		{
			List<Obs> editObsMatches = os.getObservations(Arrays.asList((Person) ps.getPatient(7)),
			    Arrays.asList(es.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac")),
			    Arrays.asList(cs.getConcept(18)), null, null, null, null, null, null, null, null, false);
			Assert.assertEquals(1, editObsMatches.size());
			Obs o = editObsMatches.get(0);
			Assert.assertEquals(false, o.getValueBoolean());
		}
		// Verif retire
		{
			Obs o = os.getObsByUuid("c87f104b-87c6-4b1d-a5fb-a951adad25c0");
			Assert.assertNotNull(o);
			Assert.assertEquals(true, o.isVoided());
		}
	}
	
}
