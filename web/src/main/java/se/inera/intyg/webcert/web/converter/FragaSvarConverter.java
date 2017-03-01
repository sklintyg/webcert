/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

/**
 * @author andreaskaltenbach
 */
@Component
public class FragaSvarConverter {

    private static final int FK_MEDDELANDE_RUBRIK_LANGD = 255;

    public FragaSvar convert(QuestionFromFkType source) {

        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
        fragaSvar.setExternReferens(source.getFkReferensId());
        fragaSvar.setAmne(Amne.valueOf(source.getAmne().value().toUpperCase()));

        if (source.getFraga() != null) {
            fragaSvar.setFrageText(source.getFraga().getMeddelandeText());
            fragaSvar.setFrageSigneringsDatum(source.getFraga().getSigneringsTidpunkt());
        }

        fragaSvar.setFrageSkickadDatum(source.getAvsantTidpunkt());
        fragaSvar.setExternaKontakter(convertFkKontaktInfo(source.getFkKontaktInfo()));
        fragaSvar.setMeddelandeRubrik(StringUtils.left(source.getFkMeddelanderubrik(), FK_MEDDELANDE_RUBRIK_LANGD));
        fragaSvar.setSistaDatumForSvar(source.getFkSistaDatumForSvar());

        fragaSvar.setIntygsReferens(convertToIntygsReferens(source.getLakarutlatande()));
        fragaSvar.setKompletteringar(convertKompletteringar(source.getFkKomplettering()));
        fragaSvar.setVardperson(convert(source.getAdressVard()));

        return fragaSvar;
    }

    private Vardperson convert(VardAdresseringsType source) {
        Vardperson vardperson = new Vardperson();
        vardperson.setHsaId(source.getHosPersonal().getPersonalId().getExtension());
        vardperson.setNamn(source.getHosPersonal().getFullstandigtNamn());
        vardperson.setForskrivarKod(source.getHosPersonal().getForskrivarkod());
        vardperson.setEnhetsId(source.getHosPersonal().getEnhet().getEnhetsId().getExtension());

        if (source.getHosPersonal().getEnhet().getArbetsplatskod() != null) {
            vardperson.setArbetsplatsKod(source.getHosPersonal().getEnhet().getArbetsplatskod().getExtension());
        }

        vardperson.setEnhetsnamn(source.getHosPersonal().getEnhet().getEnhetsnamn());
        vardperson.setPostadress(source.getHosPersonal().getEnhet().getPostadress());
        vardperson.setPostnummer(source.getHosPersonal().getEnhet().getPostnummer());
        vardperson.setPostort(source.getHosPersonal().getEnhet().getPostort());
        vardperson.setTelefonnummer(source.getHosPersonal().getEnhet().getTelefonnummer());
        vardperson.setEpost(source.getHosPersonal().getEnhet().getEpost());
        vardperson.setVardgivarId(source.getHosPersonal().getEnhet().getVardgivare().getVardgivareId().getExtension());
        vardperson.setVardgivarnamn(source.getHosPersonal().getEnhet().getVardgivare().getVardgivarnamn());

        return vardperson;
    }

    /**
     * Converts a from common models {@link HoSPersonal} to an {@link Vardperson} new instance.
     */
    public static Vardperson convert(HoSPersonal source) {
        Vardperson vardperson = new Vardperson();
        vardperson.setHsaId(source.getPersonId());
        vardperson.setNamn(source.getFullstandigtNamn());
        vardperson.setForskrivarKod(source.getForskrivarKod());
        vardperson.setEnhetsId(source.getVardenhet().getEnhetsid());

        vardperson.setArbetsplatsKod(source.getVardenhet().getArbetsplatsKod());

        vardperson.setEnhetsnamn(source.getVardenhet().getEnhetsnamn());
        vardperson.setPostadress(source.getVardenhet().getPostadress());
        vardperson.setPostnummer(source.getVardenhet().getPostnummer());
        vardperson.setPostort(source.getVardenhet().getPostort());
        vardperson.setTelefonnummer(source.getVardenhet().getTelefonnummer());
        vardperson.setEpost(source.getVardenhet().getEpost());
        vardperson.setVardgivarId(source.getVardenhet().getVardgivare().getVardgivarid());
        vardperson.setVardgivarnamn(source.getVardenhet().getVardgivare().getVardgivarnamn());

        return vardperson;
    }

    private Set<Komplettering> convertKompletteringar(List<KompletteringType> source) {
        List<Komplettering> kompletteringar = new ArrayList<>();
        for (KompletteringType kompletteringType : source) {
            Komplettering komplettering = new Komplettering();
            komplettering.setFalt(kompletteringType.getFalt());
            komplettering.setText(kompletteringType.getText());
            kompletteringar.add(komplettering);
        }
        return ImmutableSet.copyOf(kompletteringar);
    }

    private IntygsReferens convertToIntygsReferens(LakarutlatandeEnkelType source) {
        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(source.getLakarutlatandeId());
        intygsReferens.setIntygsTyp(Fk7263EntryPoint.MODULE_ID);

        if (source.getPatient() != null) {
            intygsReferens.setPatientNamn(source.getPatient().getFullstandigtNamn());
            intygsReferens.setPatientId(new Personnummer(source.getPatient().getPersonId().getExtension()));
        }

        intygsReferens.setSigneringsDatum(source.getSigneringsTidpunkt());

        return intygsReferens;
    }

    private Set<String> convertFkKontaktInfo(List<FkKontaktType> source) {
        List<String> externaKontakter = new ArrayList<>();
        for (FkKontaktType kontaktInfo : source) {
            externaKontakter.add(kontaktInfo.getKontakt());
        }
        return ImmutableSet.copyOf(externaKontakter);
    }

    /**
     * Extract / Convert from {@link Utlatande} to {@link IntygsReferens}.
     */
    public static IntygsReferens convertToIntygsReferens(Utlatande utlatande) {
        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(utlatande.getId());
        intygsReferens.setIntygsTyp(utlatande.getTyp());
        intygsReferens.setPatientId(utlatande.getGrundData().getPatient().getPersonId());
        intygsReferens.setPatientNamn(utlatande.getGrundData().getPatient().getFullstandigtNamn());
        intygsReferens.setSigneringsDatum(utlatande.getGrundData().getSigneringsdatum());
        return intygsReferens;
    }
}
