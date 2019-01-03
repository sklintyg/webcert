/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;

@Service
public class UtkastModelToXMLConverter {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastModelToXMLConverter.class);

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    public String utkastToXml(String json, String intygsTyp) {
        try {
            Intyg intyg = utkastToJAXBObject(intygsTyp, json);
            return handleAsRegisterCertificate3(intyg);
        } catch (JAXBException e) {
            LOG.error("Caught JAXBException: {}. Error code: {}", e.getMessage(), e.getErrorCode());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String handleAsRegisterCertificate3(Intyg intyg) throws JAXBException {
        RegisterCertificateType registerCertificateType = new RegisterCertificateType();
        registerCertificateType.setIntyg(intyg);

        // This context may need to be created dynamically based on the Intygstyp, given that not all intygstyper
        // are based on the same contract / domain version. Get from ModuleApi?
        JAXBContext context = JAXBContext.newInstance(RegisterCertificateType.class, DatePeriodType.class, PartialDateType.class);
        QName qname = new QName("urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3", "RegisterCertificateType");
        JAXBElement<RegisterCertificateType> root = new JAXBElement<>(qname, RegisterCertificateType.class, registerCertificateType);
        return marshalRegisterCertificateV3(context.createMarshaller(), root);
    }

    private Intyg utkastToJAXBObject(String intygsTyp, String json) {
        try {
            ModuleApi moduleApi = intygModuleRegistry.getModuleApi(intygsTyp, intygModuleRegistry.resolveVersionFromUtlatandeJson(json));
            Utlatande utlatandeFromJson = moduleApi.getUtlatandeFromJson(json);
            return moduleApi.getIntygFromUtlatande(utlatandeFromJson);
        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            LOG.error("Error building Intyg JAXB object from Utkast. Message: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String marshalRegisterCertificateV3(Marshaller marshaller, JAXBElement<RegisterCertificateType> root) throws JAXBException {
        StringWriter sw = new StringWriter();
        marshaller.marshal(root, sw);
        return sw.toString();
    }
}
