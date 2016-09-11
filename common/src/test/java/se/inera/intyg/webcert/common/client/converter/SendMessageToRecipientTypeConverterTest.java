/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.common.client.converter;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import java.time.LocalDateTime;
import org.junit.Test;

import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

public class SendMessageToRecipientTypeConverterTest {

    @Test
    public void convertTest() throws JAXBException {
        SendMessageToRecipientType request = buildSendMessageToRecipientType();
        String xmlString = SendMessageToRecipientTypeConverter.toXml(request);
        SendMessageToRecipientType requestRes = SendMessageToRecipientTypeConverter.fromXml(xmlString);
        assertEquals(request.getAmne().getCode(), requestRes.getAmne().getCode());
        assertEquals(request.getIntygsId().getExtension(), requestRes.getIntygsId().getExtension());
        assertEquals(request.getLogiskAdressMottagare(), requestRes.getLogiskAdressMottagare());
        assertEquals(request.getMeddelandeId(), requestRes.getMeddelandeId());
        assertEquals(request.getPatientPersonId().getExtension(), requestRes.getPatientPersonId().getExtension());
        assertEquals(request.getSkickatAv().getPersonalId().getExtension(), requestRes.getSkickatAv().getPersonalId().getExtension());
    }

    private SendMessageToRecipientType buildSendMessageToRecipientType() {
        SendMessageToRecipientType res = new SendMessageToRecipientType();
        res.setAmne(new Amneskod());
        res.getAmne().setCode("OVRIGT");
        res.getAmne().setCodeSystem("ffa59d8f-8d7e-46ae-ac9e-31804e8e8499");
        res.getAmne().setDisplayName("Övrigt");
        res.setIntygsId(new IntygId());
        res.getIntygsId().setRoot("TSTNMT2321000156-1039");
        res.getIntygsId().setExtension("79d77cad-0c19-4278-8212-b23c82a7e33c");
        res.setLogiskAdressMottagare("SendMessageStub");
        res.setMeddelande("svarstext meddelande");
        res.setMeddelandeId("b7360a70-80a3-4d24-b10e-621c3c0c826a");
        res.setPatientPersonId(new PersonId());
        res.getPatientPersonId().setRoot("1.2.752.129.2.1.3.1");
        res.getPatientPersonId().setExtension("19121212-1212");
        res.setReferensId("referensId");
        res.setRubrik("en fråga");
        res.setSkickatAv(new HosPersonal());
        res.getSkickatAv().setEnhet(new Enhet());
        res.getSkickatAv().getEnhet().setArbetsplatskod(new ArbetsplatsKod());
        res.getSkickatAv().getEnhet().getArbetsplatskod().setRoot("1.2.752.29.4.71");
        res.getSkickatAv().getEnhet().getArbetsplatskod().setExtension("1234567890");
        res.getSkickatAv().getEnhet().setEnhetsId(new HsaId());
        res.getSkickatAv().getEnhet().getEnhetsId().setRoot("1.2.752.129.2.1.4.1");
        res.getSkickatAv().getEnhet().getEnhetsId().setExtension("TSTNMT2321000156-1039");
        res.getSkickatAv().getEnhet().setEnhetsnamn("NMT vg1 ve2");
        res.getSkickatAv().getEnhet().setVardgivare(new Vardgivare());
        res.getSkickatAv().getEnhet().getVardgivare().setVardgivareId(new HsaId());
        res.getSkickatAv().getEnhet().getVardgivare().getVardgivareId().setRoot("1.2.752.129.2.1.4.1");
        res.getSkickatAv().getEnhet().getVardgivare().getVardgivareId().setExtension("TSTNMT2321000156-1002");
        res.getSkickatAv().getEnhet().getVardgivare().setVardgivarnamn("NMT vg1");
        res.getSkickatAv().setForskrivarkod("0000000");
        res.getSkickatAv().setFullstandigtNamn("Leonie Koehl");
        res.getSkickatAv().setPersonalId(new HsaId());
        res.getSkickatAv().getPersonalId().setRoot("1.2.752.129.2.1.4.1");
        res.getSkickatAv().getPersonalId().setExtension("TSTNMT2321000156-103F");
        res.setSkickatTidpunkt(LocalDateTime.now());
        res.setSvarPa(new MeddelandeReferens());
        res.getSvarPa().setMeddelandeId("5d665d73-7029-4619-9a91-3225a90d81c8");
        res.getSvarPa().getReferensId().add("referensid2");
        return res;
    }
}
