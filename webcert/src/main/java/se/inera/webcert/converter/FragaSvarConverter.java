package se.inera.webcert.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import se.inera.webcert.medcertqa.v1.FkKontaktType;
import se.inera.webcert.medcertqa.v1.KompletteringType;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;
import se.inera.webcert.persistence.fragasvar.model.*;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;

import com.google.common.collect.ImmutableSet;

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
        fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
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

    private IntygsReferens convert(LakarutlatandeEnkelType source) {
        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(source.getLakarutlatandeId());
        intygsReferens.setIntygsTyp("fk7263");

        if (source.getPatient() != null) {
            intygsReferens.setPatientNamn(source.getPatient().getFullstandigtNamn());

            if(source.getPatient().getPersonId()!=null){
                Id id = new Id();

                id.setPatientId(source.getPatient().getPersonId().getExtension());
                id.setPatientIdRoot(source.getPatient().getPersonId().getRoot());
                intygsReferens.setPatientId(id);
            }
        }

        return intygsReferens;
    }

    private Set<String> convertFkKontaktInfo(List<FkKontaktType> source) {
        List<String> externaKontakter = new ArrayList<>();
        for (FkKontaktType kontaktInfo : source) {
            externaKontakter.add(kontaktInfo.getKontakt());
        }
        return ImmutableSet.copyOf(externaKontakter);
    }

    public FragaSvar convert(AnswerFromFkType answer) {

        // lookup question in database
        FragaSvar fragaSvar = fragaSvarRepository.findOne(Long.parseLong(answer.getVardReferensId()));

        if (fragaSvar == null) {
            throw new IllegalStateException("No question found with internal ID " + answer.getVardReferensId());
        }

        if (FK_FRAGASTALLARE.equals(fragaSvar.getFrageStallare())) {
            throw new IllegalStateException("Incoming answer refers to question initiated by Försäkringskassan.");
        }

        // fill up FragaSvar with answer information
        fragaSvar.setSvarsText(answer.getSvar().getMeddelandeText());
        fragaSvar.setSvarSigneringsDatum(answer.getSvar().getSigneringsTidpunkt());
        fragaSvar.setSvarSkickadDatum(new LocalDateTime());

        return fragaSvar;
    }
}
