package org.openmrs.module.initializer.api.obs;

import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.initializer.api.CsvParser;

import java.io.IOException;
import java.io.InputStream;


public class ObsCsvParser extends CsvParser<Obs, ObsService, ObsLineProcessor> {

	private static final String OBS_CHANGE_MESSAGE = "Initializer";

	public ObsCsvParser(InputStream is, ObsService es) throws IOException {
		super(is, es);
	}
	
	@Override
	protected Obs save(Obs instance) {
		return service.saveObs(instance, OBS_CHANGE_MESSAGE);
	}
	
	@Override
	protected boolean isVoidedOrRetired(Obs instance) {
		return instance.getVoided();
	}
	
	@Override
	protected void setLineProcessors(String version, String[] headerLine) {
		addLineProcessor(new ObsLineProcessor(headerLine, service));
	}
}
