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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.DomainBaseModuleContextSensitiveTest;
import org.openmrs.module.initializer.InitializerConstants;
import org.openmrs.module.initializer.api.obs.ObsCsvParser;
import org.openmrs.reporting.ObsPatientFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObsStressTest extends DomainBaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("obsService")
	private ObsService os;
	
	@Override
	protected String getDomain() {
		return InitializerConstants.DOMAIN_OBS;
	}
	
	@Before
	public void setup() {
	}
	
	@Test
	public void loadObs_shouldHandleLotsOfObs() {
		InitializerService iniz = Context.getService(InitializerService.class);
		ConfigDirUtil util = new ConfigDirUtil(iniz.getDataDirPath(), iniz.getDataChecksumsDirPath(), "obsStressTest");
		List<OrderableCsvFile> files = new ArrayList<OrderableCsvFile>();
		for (File file : util.getFiles("csv")) {
			String fileName = util.getFileName(file.getPath());
			String checksum = util.getChecksumIfChanged(fileName);
			if (!checksum.isEmpty()) {
				files.add(new OrderableCsvFile(file, checksum));
			}
		}
		OrderableCsvFile file = files.get(0);
		double startTimeS = System.nanoTime() / 10E8;
		try {
			FileInputStream inputStream = new FileInputStream(file.getFile());
			ObsService os = Context.getObsService();
			ObsCsvParser ocp = new ObsCsvParser(inputStream, os);
			ocp.saveAll();
		}
		catch (FileNotFoundException e) {
			log.error(e);
		}
		catch (IOException e) {
			log.error(e);
		}
		double endTimeS = System.nanoTime() / 10E8;
		double runTimeS = endTimeS - startTimeS;
		log.warn("Test Obs took " + runTimeS + " s");
	}
}
