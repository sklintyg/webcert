package se.inera.webcert.service.intyg.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeStatus;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.util.ModelConverter;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;
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

    public IntygMetadata convertToIntygMetadata(Utlatande utlatande, CertificateMetaData meta) {
        IntygMetadata metaData = new IntygMetadata();
        metaData.setPatientId(utlatande.getGrundData().getPatient().getPersonId());
        metaData.setId(utlatande.getId());
        metaData.setType(utlatande.getTyp());
        metaData.setFromDate(meta.getValidFrom());
        metaData.setTomDate(meta.getValidTo());

        metaData.setStatuses(convertListOfStatusToListOfIntygStatus(meta.getStatus()));

        return metaData;
    }

    @Override
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

    public List<IntygStatus> convertToListOfIntygStatus(List<UtlatandeStatus> source) {
        List<IntygStatus> status = new ArrayList<>();
        for (UtlatandeStatus certificateStatusType : source) {
            status.add(convertToIntygStatus(certificateStatusType));
        }
        return status;
    }

    private IntygStatus convertToIntygStatus(UtlatandeStatus source) {
        StatusType statusType = convertStatusType(source.getType());
        return new IntygStatus(statusType, source.getTarget(), source.getTimestamp());
    }

    @Override
    public List<IntygStatus> convertListOfStatusToListOfIntygStatus(List<Status> source) {
        List<IntygStatus> intygStatus = new ArrayList<>();
        for (Status status : source) {
            intygStatus.add(convertToIntygStatus(status));
        }
        return intygStatus;
    }

    private IntygStatus convertToIntygStatus(Status source) {
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
        case RESTORED:
            return StatusType.RESTORED;
        default:
            return StatusType.UNKNOWN;
        }
    };

    private StatusType convertStatusType(CertificateState statusType) {
        switch (statusType) {
        case RECEIVED:
            return StatusType.RECEIVED;
        case SENT:
            return StatusType.SENT;
        case CANCELLED:
            return StatusType.CANCELLED;
        case DELETED:
            return StatusType.DELETED;
        case RESTORED:
            return StatusType.RESTORED;
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
        LakarutlatandeEnkelType utlatandeType = ModelConverter.toLakarutlatandeEnkelType(utlatande);

        // Vardadress
        VardAdresseringsType vardAdressType = ModelConverter.toVardAdresseringsType(utlatande.getGrundData());

        SendType sendType = new SendType();
        sendType.setLakarutlatande(utlatandeType);
        sendType.setAdressVard(vardAdressType);
        sendType.setVardReferensId(buildVardReferensId(Operation.SEND, utlatande.getId()));
        sendType.setAvsantTidpunkt(LocalDateTime.now());

        return sendType;
    }

    @Override
    public RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage) {

        // Lakarutlatande
        LakarutlatandeEnkelType utlatandeType = ModelConverter.toLakarutlatandeEnkelType(utlatande);

        // Vardadress
        VardAdresseringsType vardAdressType = ModelConverter.toVardAdresseringsType(utlatande.getGrundData());

        RevokeType revokeType = new RevokeType();
        revokeType.setLakarutlatande(utlatandeType);
        revokeType.setAdressVard(vardAdressType);
        revokeType.setVardReferensId(buildVardReferensId(Operation.REVOKE, utlatande.getId()));
        revokeType.setAvsantTidpunkt(LocalDateTime.now());

        if (revokeMessage != null) {
            revokeType.setMeddelande(revokeMessage);
        }

        return revokeType;
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

    public String buildVardReferensId(Operation op, String intygId) {
        return buildVardReferensId(op, intygId, LocalDateTime.now());
    }

    public String buildVardReferensId(Operation op, String intygId, LocalDateTime ts) {
        String time = ts.toString(ISODateTimeFormat.basicDateTime());
        return StringUtils.join(new Object[] { op, intygId, time }, "-");
    }

    public enum Operation {
        SEND,
        REVOKE;
    }
}
