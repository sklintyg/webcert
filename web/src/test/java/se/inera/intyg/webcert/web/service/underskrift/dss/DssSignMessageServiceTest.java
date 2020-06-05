/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.underskrift.dss;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import oasis.names.tc.dss._1_0.core.schema.SignRequest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.elegnamnden.id.csig._1_1.dss_ext.ns.SignRequestExtensionType;
import se.inera.intyg.infra.xmldsig.service.XMLDSigService;

@RunWith(MockitoJUnitRunner.class)
public class DssSignMessageServiceTest {

    @Mock
    XMLDSigService infraXMLDSigServiceMock;

    @InjectMocks
    private DssSignMessageService service;

    @BeforeClass
    public static void init() {
        org.apache.xml.security.Init.init();
//        System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    }

    @Test
    public void signSignRequest() {

        ReflectionTestUtils.setField(service, "keystoreAlias", "localhost");
        ReflectionTestUtils.setField(service, "keystorePassword", "password");
        ReflectionTestUtils.setField(service, "keystoreFile", new ClassPathResource("dss/localhost.p12"));

        System.out.println(service.signSignRequest(getSignRequest()));


    }

    @Test
    public void validateSignResponseSignature() {
    }

    private SignRequest getSignRequest() {
        try {
            JAXBContext jaxbContext = JAXBContext
                .newInstance(SignRequest.class, SignRequestExtensionType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new StreamSource(new ClassPathResource("dss/unsigned_signRequest.xml").getInputStream()),
                SignRequest.class).getValue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }
}