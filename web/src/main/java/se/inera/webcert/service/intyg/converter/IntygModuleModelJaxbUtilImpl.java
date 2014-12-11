package se.inera.webcert.service.intyg.converter;

import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;

@Component
public class IntygModuleModelJaxbUtilImpl implements IntygModuleModelJaxbUtil {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleModelJaxbUtilImpl.class);

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initJaxbContext() {
        try {
            this.jaxbContext = JAXBContext.newInstance(UtlatandeType.class, RegisterCertificateType.class);
        } catch (JAXBException e) {
            LOG.error("Failed to initialize JAXB Context for GetCertificate interaction", e);
            Throwables.propagate(e);
        }
    }

    private JAXBContext getJaxbContext() {

        if (this.jaxbContext == null) {
            initJaxbContext();
        }

        return this.jaxbContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.webcert.service.intyg.converter.IntygModuleModelJaxbUtil#unmarshallFromXmlToTransport(java.lang.String)
     */
    @Override
    public UtlatandeType unmarshallFromXmlToTransport(String xml) throws JAXBException {
        StreamSource source = new StreamSource(new StringReader(xml));
        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();

        JAXBElement<UtlatandeType> jaxbElement = unmarshaller.unmarshal(source, UtlatandeType.class);

        return jaxbElement.getValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.webcert.service.intyg.converter.IntygModuleModelJaxbUtil#marshallFromTransportToXml(se.inera.certificate
     * .clinicalprocess.healthcond.certificate.v1.UtlatandeType)
     */
    @Override
    public String marshallFromTransportToXml(UtlatandeType utlatandeTyp) throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBElement<UtlatandeType> jaxbElement = new ObjectFactory().createUtlatande(utlatandeTyp);
        getJaxbContext().createMarshaller().marshal(jaxbElement, writer);
        return writer.toString();
    }

}
