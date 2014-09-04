package se.inera.webcert.service.intyg.converter;

import iso.v21090.dt.v1.II;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import riv.insuranceprocess.healthreporting.medcertqa._1.LakarutlatandeEnkelType;
import riv.insuranceprocess.healthreporting.medcertqa._1.VardAdresseringsType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateStatusType;
import se.inera.certificate.model.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;

@Component
public class IntygServiceConverterImpl implements IntygServiceConverter {

    public IntygMetadata convertToIntygMetadata(String patientId, CertificateMetaType source) {

        IntygMetadata metaData = new IntygMetadata();
        metaData.setPatientId(patientId);
        metaData.setId(source.getCertificateId());
        metaData.setType(source.getCertificateType());
        metaData.setFromDate(source.getValidFrom());
        metaData.setTomDate(source.getValidTo());

        metaData.setStatuses(convertToListOfIntygStatus(source.getStatus()));

        return metaData;
    }

    public List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source) {
        List<IntygItem> intygItems = new ArrayList<>();
        for (CertificateMetaType certificateMetaType : source) {
            intygItems.add(convertToIntygItem(certificateMetaType));
        }
        return intygItems;
    }

    private IntygItem convertToIntygItem(CertificateMetaType source) {

        IntygItem item = new IntygItem();
        item.setId(source.getCertificateId());
        item.setType(source.getCertificateType());
        item.setFromDate(source.getValidFrom());
        item.setTomDate(source.getValidTo());
        item.setStatuses(convertToListOfIntygStatus(source.getStatus()));
        item.setSignedBy(source.getIssuerName());
        item.setSignedDate(source.getSignDate());

        return item;
    }

    public List<IntygStatus> convertToListOfIntygStatus(List<CertificateStatusType> source) {
        List<IntygStatus> status = new ArrayList<>();
        for (CertificateStatusType certificateStatusType : source) {
            status.add(convertToIntygStatus(certificateStatusType));
        }
        return status;
    }

    private IntygStatus convertToIntygStatus(CertificateStatusType source) {
        StatusType statusType = convertStatusType(source.getType());
        return new IntygStatus(statusType, source.getTarget(), source.getTimestamp());
    }

    private StatusType convertStatusType(se.inera.certificate.clinicalprocess.healthcond.certificate.v1.StatusType statusType) {
        switch (statusType) {
        case RECEIVED:
            return StatusType.RECEIVED;
        case SENT:
            return StatusType.SENT;
        case CANCELLED:
            return StatusType.CANCELLED;
        case DELETED:
            return StatusType.DELETED;
        default:
            return StatusType.UNKNOWN;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.intyg.converter.IntygServiceConverter#buildSendTypeFromUtlatande(se.inera.certificate
     * .model.Utlatande)
     */
    @Override
    public SendType buildSendTypeFromUtlatande(Utlatande utlatande) {

        // Lakarutlatande
        PatientType patientType = new PatientType();
        patientType.setFullstandigtNamn(concatPatientName(utlatande.getPatient().getFornamn(), utlatande.getPatient().getMellannamn(),
                utlatande.getPatient().getEfternamn()));

        II personId = new II();
        personId.setRoot(utlatande.getPatient().getId().getRoot());
        personId.setExtension(utlatande.getPatient().getId().getExtension());

        patientType.setPersonId(personId);

        LakarutlatandeEnkelType utlatandeType = new LakarutlatandeEnkelType();
        utlatandeType.setLakarutlatandeId((utlatande.getId().getExtension() != null ? utlatande.getId().getExtension() :
                utlatande.getId().getRoot()));
        utlatandeType.setPatient(patientType);
        utlatandeType.setSigneringsTidpunkt(utlatande.getSigneringsdatum());

        // Vardadress
        HosPersonalType hosPersonalType = new HosPersonalType();
        hosPersonalType.setEnhet(buildEnhetFromUtlatande(utlatande));
        II hosPersonId = new II();
        hosPersonId.setExtension(utlatande.getSkapadAv().getId().getExtension());
        hosPersonId.setRoot(utlatande.getSkapadAv().getId().getRoot());
        hosPersonalType.setPersonalId(hosPersonId);
        hosPersonalType.setFullstandigtNamn(utlatande.getSkapadAv().getNamn());
        hosPersonalType.setForskrivarkod(utlatande.getSkapadAv().getForskrivarkod());

        VardAdresseringsType vardAdressType = new VardAdresseringsType();
        vardAdressType.setHosPersonal(hosPersonalType);

        SendType sendType = new SendType();
        sendType.setLakarutlatande(utlatandeType);
        sendType.setAdressVard(vardAdressType);
        sendType.setVardReferensId("WC");
        sendType.setAvsantTidpunkt(LocalDateTime.now());

        return sendType;
    }

    private EnhetType buildEnhetFromUtlatande(Utlatande utlatande) {

        EnhetType enhet = new EnhetType();
        enhet.setEnhetsnamn(utlatande.getSkapadAv().getVardenhet().getNamn());

        II enhetsId = new II();
        enhetsId.setRoot(utlatande.getSkapadAv().getVardenhet().getId().getRoot());
        enhetsId.setExtension(utlatande.getSkapadAv().getVardenhet().getId().getExtension());
        enhet.setEnhetsId(enhetsId);

        if (utlatande.getSkapadAv().getVardenhet().getArbetsplatskod() != null) {
            II arbetsplatsKod = new II();
            arbetsplatsKod.setRoot(utlatande.getSkapadAv().getVardenhet().getArbetsplatskod().getRoot());
            arbetsplatsKod.setExtension(utlatande.getSkapadAv().getVardenhet().getArbetsplatskod().getExtension());
            enhet.setArbetsplatskod(arbetsplatsKod);
        }

        VardgivareType vardGivare = new VardgivareType();

        II vardGivarId = new II();
        vardGivarId.setRoot(utlatande.getSkapadAv().getVardenhet().getVardgivare().getId().getRoot());
        vardGivarId.setExtension(utlatande.getSkapadAv().getVardenhet().getVardgivare().getId().getExtension());
        vardGivare.setVardgivareId(vardGivarId);

        vardGivare.setVardgivarnamn(utlatande.getSkapadAv().getVardenhet().getVardgivare().getNamn());
        enhet.setVardgivare(vardGivare);

        return enhet;
    }

    public String concatPatientName(List<String> fNames, List<String> mNames, String lName) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.join(fNames, " "));

        if (!mNames.isEmpty()) {
            sb.append(" ").append(StringUtils.join(mNames, " "));
        }

        sb.append(" ").append(lName);
        return StringUtils.normalizeSpace(sb.toString());
    }
}
