package se.inera.webcert.converter;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import se.inera.certificate.model.common.internal.HoSPersonal;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.FkKontaktType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.KompletteringType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author andreaskaltenbach
 */
@Component
public class FragaSvarConverter {

    private static final String FK_FRAGASTALLARE = "FK";

    private static final int FK_MEDDELANDE_RUBRIK_LANGD = 255;

    public FragaSvar convert(QuestionFromFkType source) {

        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setFrageStallare(FK_FRAGASTALLARE);
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
        intygsReferens.setIntygsTyp("fk7263");

        if (source.getPatient() != null) {
            intygsReferens.setPatientNamn(source.getPatient().getFullstandigtNamn());
            intygsReferens.setPatientId(source.getPatient().getPersonId().getExtension());
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
     *
     * @param utlatande
     * @return
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

    /*
    private static Id toCommonId(Patient patient) {
        return new Id(patient.isSamordningsNummer() ? Constants.SAMORDNING_ID_OID : Constants.PERSON_ID_OID, patient.getPersonId());
    }
    */

}
