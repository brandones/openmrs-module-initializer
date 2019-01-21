package org.openmrs.module.initializer.api.obs;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;

public class ObsLineProcessor extends BaseLineProcessor<Obs, ObsService> {
	
	public static final String HEADER_DATE = "Date";

	public ObsLineProcessor(String[] headerLine, ObsService es) {
		super(headerLine, es);
	}
	
	@Override
	protected Obs bootstrap(CsvLine line) throws IllegalArgumentException {
		String uuid = getUuid(line.asLine());
		Obs enc = service.getObsByUuid(uuid);
		
		if (enc == null) {
			enc = new Obs();
			if (!StringUtils.isEmpty(uuid)) {
				enc.setUuid(uuid);
			}
		}
		
		enc.setVoided(getVoidOrRetire(line.asLine()));
		
		return enc;
	}
	
	@Override
	protected Obs fill(Obs obs, CsvLine line) throws IllegalArgumentException {
	    return obs;
	}
	
}
