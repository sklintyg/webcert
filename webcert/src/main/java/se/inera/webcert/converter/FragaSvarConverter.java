package se.inera.webcert.converter;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import se.inera.webcert.medcertqa.v1.FkKontaktType;
import se.inera.webcert.medcertqa.v1.KompletteringType;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;

/**
 * @author andreaskaltenbach
 */
@Component
public class FragaSvarConverter {

    private static final String FK_FRAGASTALLARE = "FK";

    @Autowired
    private CrudRepository<FragaSvar, Long> fragaSvarRepository;

    public FragaSvar convert(QuestionFromFkType source) {

        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setFrageStallare(FK_FRAGASTALLARE);
        fragaSvar.setExternReferens(source.getFkReferensId());
        fragaSvar.setAmne(Amne.valueOf(source.getAmne().value().toUpperCase()));

        if (source.getFraga() != null) {
            fragaSvar.setFrageText(source.getFraga().getMeddelandeText());
            fragaSvar.setFrageSigneringsDatum(source.getFraga().getSigneringsTidpunkt());
        }

        fragaSvar.setFrageSkickadDatum(source.getAvsantTidpunkt());
        fragaSvar.setExternaKontakter(convertFkKontaktInfo(source.getFkKontaktInfo()));
        fragaSvar.setMeddelandeRubrik(source.getFkMeddelanderubrik());
        fragaSvar.setSistaDatumForSvar(source.getFkSistaDatumForSvar());

        fragaSvar.setIntygsReferens(convert(source.getLakarutlatande()));
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
        vardperson.setVardgivarId(source.getHosPersonal().getEnhet().getVardgivare().getVardgivarnamn());

        return vardperson;
    }

    private List<Komplettering> convertKompletteringar(List<KompletteringType> source) {
        List<Komplettering> kompletteringar = new ArrayList<>();
        for (KompletteringType kompletteringType : source) {
            Komplettering komplettering = new Komplettering();
            komplettering.setFalt(kompletteringType.getFalt());
            komplettering.setText(kompletteringType.getText());
            kompletteringar.add(komplettering);
        }
        return kompletteringar;
    }

    private IntygsReferens convert(LakarutlatandeEnkelType source) {
        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(source.getLakarutlatandeId());
        intygsReferens.setIntygsTyp("fk7263");

        if (source.getPatient() != null) {
            intygsReferens.setPatientNamn(source.getPatient().getFullstandigtNamn());
            intygsReferens.setPatientPersonNummer(source.getPatient().getPersonId().getExtension());
        }

        return intygsReferens;
    }

    private List<String> convertFkKontaktInfo(List<FkKontaktType> source) {
        List<String> externaKontakter = new ArrayList<>();
        for (FkKontaktType kontaktInfo : source) {
            externaKontakter.add(kontaktInfo.getKontakt());
        }
        return externaKontakter;
    }

    public FragaSvar convert(AnswerFromFkType answer) {

        // lookup question in database
        FragaSvar fragaSvar = fragaSvarRepository.findOne(Long.parseLong(answer.getVardReferensId()));

        if (fragaSvar == null) {
            throw new IllegalStateException("No question found with internal ID " + answer.getVardReferensId());
        }

        if (FK_FRAGASTALLARE.equals(fragaSvar.getFrageStallare())) {
            throw new IllegalStateException("Incoming answer referns to question initiated by Försäkringskassan.");
        }

        // fill up FragaSvar with answer information
        fragaSvar.setSvarsText(answer.getSvar().getMeddelandeText());
        fragaSvar.setSvarSigneringsDatum(answer.getSvar().getSigneringsTidpunkt());
        fragaSvar.setSvarSkickadDatum(new LocalDateTime());

        return fragaSvar;
    }
}
