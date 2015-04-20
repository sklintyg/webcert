package se.inera.webcert.fkstub.validation;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;

import java.util.List;


/**
 * @author andreaskaltenbach
 */
public class VardAdresseringsTypeValidator {

    private final VardAdresseringsType vardAdress;
    private final List<String> validationErrors;

    private static final String HOS_PERSONAL_OID = "1.2.752.129.2.1.4.1";
    private static final String ENHET_OID = "1.2.752.129.2.1.4.1";

    public VardAdresseringsTypeValidator(VardAdresseringsType vardAdress, List<String> validationErrors) {
        this.vardAdress = vardAdress;
        this.validationErrors = validationErrors;
    }

    public void validateAndCorrect() {
        if (vardAdress == null) {
            validationErrors.add("No vardAdress element found!");
            return;
        }

        HosPersonalType hosPersonal = vardAdress.getHosPersonal();
        if (hosPersonal == null) {
            validationErrors.add("No SkapadAvHosPersonal element found!");
            return;
        }

        // Check lakar id - mandatory
        if (hosPersonal.getPersonalId().getExtension() == null || hosPersonal.getPersonalId().getExtension().isEmpty()) {
            validationErrors.add("No personal-id found!");
        }

        // Check lakar id o.i.d.
        if (hosPersonal.getPersonalId().getRoot() == null
                || !hosPersonal.getPersonalId().getRoot().equals(HOS_PERSONAL_OID)) {
            validationErrors.add("Wrong o.i.d. for personalId! Should be " + HOS_PERSONAL_OID);
        }

        // Check lakarnamn - mandatory
        if (hosPersonal.getFullstandigtNamn() == null || hosPersonal.getFullstandigtNamn().isEmpty()) {
            validationErrors.add("No skapadAvHosPersonal fullstandigtNamn found.");
        }

        validateHosPersonalEnhet(hosPersonal.getEnhet());
    }

    private void validateHosPersonalEnhet(EnhetType enhet) {
        if (enhet == null) {
            validationErrors.add("No enhet element found!");
            return;
        }

        // Check enhets id - mandatory
        if (enhet.getEnhetsId() == null || enhet.getEnhetsId().getExtension() == null
                || enhet.getEnhetsId().getExtension().isEmpty()) {
            validationErrors.add("No enhets-id found!");
        }

        // Check enhets o.i.d
        if (enhet.getEnhetsId() == null || enhet.getEnhetsId().getRoot() == null
                || !enhet.getEnhetsId().getRoot().equals(ENHET_OID)) {
            validationErrors.add("Wrong o.i.d. for enhetsId! Should be " + ENHET_OID);
        }

        // Check enhetsnamn - mandatory
        if (enhet.getEnhetsnamn() == null || enhet.getEnhetsnamn().length() < 1) {
            validationErrors.add("No enhetsnamn found!");
        }

        validateVardgivare(enhet.getVardgivare());
    }

    private void validateVardgivare(VardgivareType vardgivare) {
        if (vardgivare == null) {
            validationErrors.add("No vardgivare element found!");
            return;
        }

        // Check vardgivare id - mandatory
        if (vardgivare.getVardgivareId() == null || vardgivare.getVardgivareId().getExtension() == null
                || vardgivare.getVardgivareId().getExtension().isEmpty()) {
            validationErrors.add("No vardgivare-id found!");
        }
        // Check vardgivare o.i.d.
        if (vardgivare.getVardgivareId() == null || vardgivare.getVardgivareId().getRoot() == null
                || !vardgivare.getVardgivareId().getRoot().equals(HOS_PERSONAL_OID)) {
            validationErrors.add("Wrong o.i.d. for vardgivareId! Should be " + HOS_PERSONAL_OID);
        }

        // Check vardgivarename - mandatory
        if (vardgivare.getVardgivarnamn() == null || vardgivare.getVardgivarnamn().isEmpty()) {
            validationErrors.add("No vardgivarenamn found!");
        }
    }
}
