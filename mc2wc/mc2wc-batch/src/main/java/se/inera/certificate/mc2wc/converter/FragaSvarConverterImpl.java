package se.inera.certificate.mc2wc.converter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.inera.certificate.mc2wc.message.AnswerType;
import se.inera.certificate.mc2wc.message.CareGiverType;
import se.inera.certificate.mc2wc.message.CarePersonType;
import se.inera.certificate.mc2wc.message.CareUnitType;
import se.inera.certificate.mc2wc.message.QuestionOriginatorType;
import se.inera.certificate.mc2wc.message.QuestionSubjectType;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.certificate.mc2wc.message.StatusType;
import se.inera.certificate.mc2wc.message.SupplementType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Id;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

public class FragaSvarConverterImpl implements FragaSvarConverter {
    
    private static final String PERSONNUMMER_OID = "1.2.752.129.2.1.3.1";
    private static final String SAMORDNINGSNUMMER_OID = "1.2.752.129.2.1.3.3";
    
    @Override
    public FragaSvar toFragaSvar(QuestionType qa) {

        FragaSvar fs = new FragaSvar();

        IntygsReferens intygsRef = new IntygsReferens();
        intygsRef.setIntygsId(qa.getCertificateId());
        intygsRef.setIntygsTyp(qa.getCertificateType()); // TODO: hardcode ?
        
        Id patientId = toPatientId(qa.getPatient().getPersonId());
        
        intygsRef.setPatientId(patientId);
        intygsRef.setPatientNamn(qa.getPatient().getFullName());
        intygsRef.setSigneringsDatum(qa.getCertificateSigned());

        fs.setIntygsReferens(intygsRef);
        fs.setFrageStallare(toFrageStallare(qa.getOriginator()));

        fs.setAmne(toAmne(qa.getSubject()));
        fs.setMeddelandeRubrik(qa.getCaption());
        fs.setFrageText(qa.getQuestionText());
        fs.setFrageSigneringsDatum(qa.getSigned());
        fs.setFrageSkickadDatum(qa.getSent());
        fs.setSistaDatumForSvar(qa.getQuestionLastAnswerDate());

        AnswerType answer = qa.getAnswer();
        if (answer != null) {
            fs.setSvarsText(answer.getText());
            fs.setSvarSigneringsDatum(answer.getSigned());
            fs.setSvarSkickadDatum(answer.getSent());
        }

        Vardperson vardperson = convertToVardperson(qa.getCarePerson());
        fs.setVardperson(vardperson);

        fs.setExternReferens(qa.getExternalReference());

        Set<Komplettering> kompletteringar = convertSupplements(qa.getSupplements());
        fs.setKompletteringar(kompletteringar);

        Status status = calculateStatus(qa);
        fs.setStatus(status);


        //fs.setExternaKontakter(externaKontakter);

        return fs;
    }

    private Id toPatientId(String patientId) {
        String patientIdRoot = FragaSvarUtils.detectIfSamordningsNummer(patientId) ? SAMORDNINGSNUMMER_OID : PERSONNUMMER_OID; 
        return new Id(patientIdRoot, patientId);
    }

    private String toFrageStallare(QuestionOriginatorType originator) {

        if (originator == null) {
            return null;
        }

        if (originator.equals(QuestionOriginatorType.CARE)) {
            return "WC";
        } else if (originator.equals(QuestionOriginatorType.FK)) {
            return "FK";
        }

        return null;
    }

    private Vardperson convertToVardperson(CarePersonType cp) {

        Vardperson vp = new Vardperson();

        vp.setNamn(cp.getFullName());
        vp.setHsaId(cp.getPersonId());
        vp.setForskrivarKod(cp.getPrescriptionCode());

        CareUnitType cu = cp.getCareUnit();
        vp.setArbetsplatsKod(cu.getWorkplaceCode());
        vp.setEnhetsId(cu.getId());
        vp.setEnhetsnamn(cu.getName());
        vp.setPostadress(cu.getPostalAddress());
        vp.setPostnummer(cu.getPostalNumber());
        vp.setPostort(cu.getPostalCity());
        vp.setTelefonnummer(cu.getPhone());
        vp.setEpost(cu.getEmail());

        CareGiverType cg = cu.getCareGiver();
        vp.setVardgivarId(cg.getId());
        vp.setVardgivarnamn(cg.getName());

        return vp;
    }

    private Set<Komplettering> convertSupplements(List<SupplementType> supplements) {

        if (supplements == null || supplements.isEmpty()) {
            return new HashSet<Komplettering>(0);
        }

        Set<Komplettering> kompletteringar = new HashSet<Komplettering>();

        for (SupplementType s : supplements) {

            Komplettering k = new Komplettering();

            k.setFalt(s.getField());
            k.setText(s.getText());

            kompletteringar.add(k);
        }

        return kompletteringar;
    }

    private Status calculateStatus(QuestionType qa) {

        QuestionOriginatorType originator = qa.getOriginator();
        boolean gotAnswer = (qa.getAnswer() != null);

        StatusType questionStatus = qa.getStatus();
        StatusType answerStatus = (gotAnswer) ? qa.getAnswer().getStatus() : null;

        if (originator.equals(QuestionOriginatorType.CARE)) {
            if (gotAnswer) {
                return Status.ANSWERED;
            }

            return Status.PENDING_EXTERNAL_ACTION;

        } else if (originator.equals(QuestionOriginatorType.FK)) {
            if (gotAnswer) {
                return Status.PENDING_INTERNAL_ACTION;
            }

            return Status.ANSWERED;
        }

        return null;
    }

    private Amne toAmne(QuestionSubjectType questionSubject) {
        if (questionSubject == null) {
            return Amne.OVRIGT;
        }
        switch (questionSubject) {
            case CONTACT:
                return Amne.KONTAKT;
            case KOMPLEMENTING:
                return Amne.KOMPLETTERING_AV_LAKARINTYG;
            case MAKULERING:
                return Amne.MAKULERING_AV_LAKARINTYG;
            case MEETING:
                return Amne.AVSTAMNINGSMOTE;
            case REMINDER:
                return Amne.PAMINNELSE;
            case WORK_PROLONGING:
                return Amne.ARBETSTIDSFORLAGGNING;
            case OTHER:
                return Amne.OVRIGT;
            default:
                return null;
        }

    }

}
