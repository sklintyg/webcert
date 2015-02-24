package se.inera.webcert.service.intyg.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.schema.util.ClinicalProcessCertificateMetaTypeConverter;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.util.ModelConverter;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;
import se.inera.webcert.service.intyg.dto.IntygItem;

@Component
public class IntygServiceConverterImpl implements IntygServiceConverter {

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
        item.setStatuses(ClinicalProcessCertificateMetaTypeConverter.toStatusList(source.getStatus()));
        item.setSignedBy(source.getIssuerName());
        item.setSignedDate(source.getSignDate());

        return item;
    }

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
