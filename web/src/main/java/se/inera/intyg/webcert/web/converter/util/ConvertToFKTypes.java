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

package se.inera.intyg.webcert.web.converter.util;

import static se.inera.intyg.common.support.Constants.ARBETSPLATS_KOD_OID;
import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.common.support.Constants.PERSON_ID_OID;
import static se.inera.intyg.common.support.Constants.SAMORDNING_ID_OID;

import org.joda.time.LocalDateTime;

import iso.v21090.dt.v1.II;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.v2.*;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;

/**
 * Created by pehr on 10/2/13.
 */
public final class ConvertToFKTypes {

    private ConvertToFKTypes() {
    }

    public static II toII(String root, String ext) {
        if ((root == null) || (ext == null)) {
            return null;
        }
        II ii = new II();
        ii.setRoot(root);
        ii.setExtension(ext);
        return ii;
    }

    public static Amnetyp toAmneTyp(Amne amne) {
        switch (amne) {
        case ARBETSTIDSFORLAGGNING:
            return Amnetyp.ARBETSTIDSFORLAGGNING;
        case AVSTAMNINGSMOTE:
            return Amnetyp.AVSTAMNINGSMOTE;
        case KOMPLETTERING_AV_LAKARINTYG:
            return Amnetyp.KOMPLETTERING_AV_LAKARINTYG;
        case KONTAKT:
            return Amnetyp.KONTAKT;
        case MAKULERING_AV_LAKARINTYG:
            return Amnetyp.MAKULERING_AV_LAKARINTYG;
        case OVRIGT:
            return Amnetyp.OVRIGT;
        case PAMINNELSE:
            return Amnetyp.PAMINNELSE;
        default:
            return null;
        }

    }

    public static InnehallType toInnehallType(String text, LocalDateTime singeringsDatum) {
        InnehallType iht = new InnehallType();
        iht.setMeddelandeText(text);
        if (singeringsDatum != null) {
            iht.setSigneringsTidpunkt(singeringsDatum);
        }
        return iht;
    }

    public static LakarutlatandeEnkelType toLakarUtlatande(IntygsReferens ir) {
        if (ir == null) {
            return null;
        }

        LakarutlatandeEnkelType lu = new LakarutlatandeEnkelType();
        lu.setLakarutlatandeId(ir.getIntygsId());

        PatientType pt = new PatientType();
        pt.setFullstandigtNamn(ir.getPatientNamn());

        String root = PERSON_ID_OID;
        if (ir.getPatientId().isSamordningsNummer()) {
            root = SAMORDNING_ID_OID;
        }

        pt.setPersonId(toII(root, ir.getPatientId().getPersonnummer()));

        lu.setPatient(pt);
        lu.setSigneringsTidpunkt(ir.getSigneringsDatum());

        return lu;
    }

    public static VardAdresseringsType toVardAdresseringsType(Vardperson vp) {
        if (vp == null) {
            return null;
        }

        VardAdresseringsType vat = new VardAdresseringsType();

        HosPersonalType hos = new HosPersonalType();
        hos.setForskrivarkod(vp.getForskrivarKod());
        hos.setFullstandigtNamn(vp.getNamn());
        hos.setPersonalId(toII(HSA_ID_OID, vp.getHsaId()));

        hos.setEnhet(toEnhetType(vp));

        vat.setHosPersonal(hos);

        return vat;
    }

    public static EnhetType toEnhetType(Vardperson vp) {
        if (vp == null) {
            return null;
        }
        EnhetType et = new EnhetType();
        if (vp.getEnhetsId() != null) {
            et.setEnhetsId(toII(HSA_ID_OID, vp.getEnhetsId()));
        }

        et.setEnhetsnamn(vp.getEnhetsnamn());
        et.setEpost(vp.getEpost());
        et.setPostadress(vp.getPostadress());
        et.setPostnummer(vp.getPostnummer());
        et.setPostort(vp.getPostort());
        et.setTelefonnummer(vp.getTelefonnummer());

        if (vp.getArbetsplatsKod() != null) {
            et.setArbetsplatskod(toII(ARBETSPLATS_KOD_OID, vp.getArbetsplatsKod()));
        }

        VardgivareType vgt = new VardgivareType();
        if (vp.getVardgivarnamn() != null) {
            vgt.setVardgivarnamn(vp.getVardgivarnamn());
        }

        if (vp.getVardgivarId() != null) {
            vgt.setVardgivareId(toII(HSA_ID_OID, vp.getVardgivarId()));
        }

        et.setVardgivare(vgt);

        return et;
    }

}
