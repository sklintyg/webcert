/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.testutil;

import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.ReferenceType;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2000._09.xmldsig_.SignedInfoType;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

public class UnderskriftTestUtil {

    public static final String INTYG_ID = "intyg-1";
    public static final String INTYG_TYP = "luse";
    public static final String ENHET_ID = "enhet-1";
    public static final String PERSON_ID = "19121212-1212";
    public static final String TICKET_ID = "ticket-1";
    public static final Long VERSION = 1L;

    public static Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model,
            VardpersonReferens vardperson, String enhetsId, String personId) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setEnhetsId(enhetsId);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(personId).get());

        return utkast;
    }

    public static SignaturBiljett createSignaturBiljett(SignaturStatus status) {
        return SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett(TICKET_ID, SignaturTyp.XMLDSIG)
                .withIntygsId(INTYG_ID)
                .withVersion(VERSION)
                .withStatus(status)
                .withSkapad(LocalDateTime.now().minusSeconds(30L))
                .withHash("hash")
                .withIntygSignature(buildIntygXMLSignature())
                .build();
    }

    public static  IntygXMLDSignature buildIntygXMLSignature() {
        return IntygXMLDSignature.IntygXMLDSignatureBuilder.anIntygXMLDSignature()
                .withSignatureType(buildSignature())
                .withSignedInfoForSigning("<SignedInfo/>")
                .withIntygJson("json")
                .withCanonicalizedIntygXml("<intyg/>")
                .build();
    }

    private static SignatureType buildSignature() {
        ObjectFactory of = new ObjectFactory();
        SignatureType st = of.createSignatureType();
        SignedInfoType signedInfo = of.createSignedInfoType();
        ReferenceType reference = of.createReferenceType();
        reference.setDigestValue("digest".getBytes(Charset.forName("UTF-8")));
        signedInfo.getReference().add(reference);
        st.setSignedInfo(signedInfo);
        return st;
    }

    public static VardpersonReferens createVardperson() {

        HoSPersonal hoSPerson = createHoSPerson();
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getPersonId());
        vardperson.setNamn(hoSPerson.getFullstandigtNamn());

        return vardperson;
    }

    public static HoSPersonal createHoSPerson() {
        HoSPersonal hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId("AAA");
        hoSPerson.setFullstandigtNamn("Dr Dengroth");
        return hoSPerson;
    }
}
