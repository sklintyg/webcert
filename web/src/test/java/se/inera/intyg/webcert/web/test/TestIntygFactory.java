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
package se.inera.intyg.webcert.web.test;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Util for building test data.
 *
 * @author nikpet
 *
 */
public final class TestIntygFactory {

    private TestIntygFactory() {

    }

    public static List<ListIntygEntry> createListWithIntygItems() {

        List<ListIntygEntry> list = new ArrayList<>();

        list.add(createIntygItem("3", LocalDateTime.parse("2014-01-02T10:11:23")));
        list.add(createIntygItem("4", LocalDateTime.parse("2014-01-03T12:12:18")));

        return list;
    }

    public static ListIntygEntry createIntygItem(String id, LocalDateTime signedDate) {

        ListIntygEntry it = new ListIntygEntry();

        it.setIntygId(id);
        it.setUpdatedSignedBy("A Person");
        it.setLastUpdatedSigned(signedDate);
        it.setIntygType("Type 1");

        it.setStatus(CertificateState.SENT.name());

        return it;
    }

    public static List<Utkast> createListWithUtkast() {

        List<Utkast> list = new ArrayList<>();

        list.add(createUtkast("2", LocalDateTime.parse("2014-01-01T10:00:00")));
        list.add(createUtkast("1", LocalDateTime.parse("2014-01-01T08:00:00")));

        return list;
    }

    public static Utkast createUtkast(String id, LocalDateTime lastUpdated) {
        return createUtkast(id, lastUpdated, "A Type", "A Person", "HSA1234", UtkastStatus.DRAFT_COMPLETE, new Personnummer("19121212-1212"));
    }

    public static Utkast createUtkast(String id, LocalDateTime lastUpdated, String type, String modifiedBy, String modifiedByHsaId,
                                      UtkastStatus status, Personnummer patientId) {

        VardpersonReferens vp = new VardpersonReferens();
        vp.setNamn(modifiedBy);
        vp.setHsaId(modifiedByHsaId);

        Utkast utkast = new Utkast();

        utkast.setIntygsId(id);
        utkast.setSenastSparadAv(vp);
        utkast.setSkapadAv(vp);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setSenastSparadDatum(lastUpdated);
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvsson");
        utkast.setPatientPersonnummer(patientId);

        return utkast;
    }

}
