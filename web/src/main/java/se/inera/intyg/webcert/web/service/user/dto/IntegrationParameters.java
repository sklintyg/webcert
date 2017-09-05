package se.inera.intyg.webcert.web.service.user.dto;

import java.io.Serializable;

public final class IntegrationParameters implements Serializable {
    private final String reference;
    private final String responsibleHospName;
    private final String alternateSsn;
    private String beforeAlternateSsn; // Sätts när alternateSsn skiljer från intygets patientId för att kunna visa det i utkastet.
    private final String fornamn;
    private final String mellannamn;
    private final String efternamn;
    private final String postadress;
    private final String postnummer;
    private final String postort;
    private final boolean sjf; //Sammanhållen JournalFöring
    private final boolean patientDeceased;
    private final boolean inactiveUnit;
    private final boolean copyOk;

    // CHECKSTYLE:OFF ParameterNumber
    public IntegrationParameters(String reference, String responsibleHospName, String alternateSsn, String fornamn, String mellannamn,
            String efternamn, String postadress, String postnummer, String postort, boolean sjf, boolean patientDeceased,
            boolean inactiveUnit, boolean copyOk) {
        this.reference = reference;
        this.responsibleHospName = responsibleHospName;
        this.alternateSsn = alternateSsn;
        this.fornamn = fornamn;
        this.mellannamn = mellannamn;
        this.efternamn = efternamn;
        this.postadress = postadress;
        this.postnummer = postnummer;
        this.postort = postort;
        this.sjf = sjf;
        this.patientDeceased = patientDeceased;
        this.inactiveUnit = inactiveUnit;
        this.copyOk = copyOk;
    }
    // CHECKSTYLE:ON ParameterNumber

    public String getReference() {
        return reference;
    }

    public String getResponsibleHospName() {
        return responsibleHospName;
    }

    public String getAlternateSsn() {
        return alternateSsn;
    }

    public void setBeforeAlternateSsn(String beforeAlternateSsn) {
        this.beforeAlternateSsn = beforeAlternateSsn;
    }

    public String getBeforeAlternateSsn() {
        return beforeAlternateSsn;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getMellannamn() {
        return mellannamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public String getPostadress() {
        return postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public boolean isSjf() {
        return sjf;
    }

    public boolean isPatientDeceased() {
        return patientDeceased;
    }

    public boolean isInactiveUnit() {
        return inactiveUnit;
    }

    public boolean isCopyOk() {
        return copyOk;
    }

}
