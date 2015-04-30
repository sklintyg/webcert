package se.inera.certificate.spec.util

import fitnesse.slim.converters.ConverterRegistry

class Config {

	String property
	String value
	
    Config() {
        ConverterRegistry.addConverter(String.class, new StringConverter());
    }

	void execute() {
		if (value && !value.contains("undefined variable:")) System.setProperty(property, value)
	}
}
