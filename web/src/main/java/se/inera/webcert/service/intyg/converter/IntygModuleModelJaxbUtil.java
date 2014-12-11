package se.inera.webcert.service.intyg.converter;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;

import javax.xml.bind.JAXBException;

public interface IntygModuleModelJaxbUtil {

    UtlatandeType unmarshallFromXmlToTransport(String xml) throws JAXBException;

    String marshallFromTransportToXml(UtlatandeType utlatandeTyp) throws JAXBException;
}
