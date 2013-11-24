package com.kiva.partner;

import java.util.List;
import org.apache.log4j.xml.DOMConfigurator;

public class KivaService {

	static {
		DOMConfigurator.configure("log4j.xml");
	}

	public static void main(String[] args) {
		KivaClient kc = new KivaClient();
		List<Partner> partners = kc.getPartners();

		SFClient pc = new SFClient();
		pc.insertLead(partners);
	}

}
