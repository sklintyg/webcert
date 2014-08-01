package se.inera.webcert.service.intyg.converter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;

public class IntygModuleModelJaxbUtilTest {
    
    private IntygModuleModelJaxbUtilImpl jaxbUtil;
    
    @Before
    public void setup() {
        jaxbUtil = new IntygModuleModelJaxbUtilImpl();
        jaxbUtil.initJaxbContext();
    }
    
    @Test
    public void testMarshall() throws JAXBException, IOException {
        
        UtlatandeType utlatandeType = getUtlatandeType();
        String res = jaxbUtil.marshallFromTransportToXml(utlatandeType);
        assertNotNull(res);
    }
    
    @Test
    public void testUnmarshall() throws JAXBException, IOException {
        
        String xml = getUtlatandeAsXml();
        UtlatandeType res = jaxbUtil.unmarshallFromXmlToTransport(xml);
        assertNotNull(res);
    }
    
    private String getUtlatandeAsXml() throws IOException {
        return IOUtils.toString(new ClassPathResource("IntygServiceTest/utlatande-transport.xml").getInputStream(), "UTF-8");
    }

    private UtlatandeType getUtlatandeType() throws JAXBException, IOException {
        StreamSource intygSource = new StreamSource(new ClassPathResource("IntygServiceTest/utlatande-transport.xml").getInputStream());
        JAXBContext context = JAXBContext.newInstance(UtlatandeType.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        UtlatandeType utlatandeType = unmarshaller.unmarshal(intygSource, UtlatandeType.class).getValue();
        return utlatandeType;
    }
}
