package se.inera.webcert.service.intyg.converter;

import javax.xml.bind.JAXBException;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;

public interface IntygModuleModelJaxbUtil {

    public abstract UtlatandeType unmarshallFromXmlToTransport(String xml) throws JAXBException;

    public abstract String marshallFromTransportToXml(UtlatandeType utlatandeTyp) throws JAXBException;

}
