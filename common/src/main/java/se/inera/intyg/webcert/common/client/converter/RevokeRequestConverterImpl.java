package se.inera.intyg.webcert.common.client.converter;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Component;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;

/**
 * Created by eriklupander on 2015-05-21.
 */
@Component
public class RevokeRequestConverterImpl implements RevokeRequestConverter {

    private static final String UTF_8 = "UTF-8";

    private ObjectFactory objectFactory;
    private JAXBContext jaxbContext;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    public String toXml(RevokeMedicalCertificateRequestType request) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RevokeMedicalCertificateRequestType> requestElement = objectFactory
                .createRevokeMedicalCertificateRequest(request);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    @Override
    public RevokeMedicalCertificateRequestType fromXml(String xml) throws JAXBException {
        JAXBElement<RevokeMedicalCertificateRequestType> unmarshalledObject = (JAXBElement<RevokeMedicalCertificateRequestType>)
                jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xml.getBytes(Charset.forName(UTF_8))));
        return unmarshalledObject.getValue();
    }
}
