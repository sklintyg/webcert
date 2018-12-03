package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.schemas.contract.Personnummer;

public final class MaximalSjukskrivningstidRequest {

    private Icd10KoderRequest icd10Koder;
    private Personnummer personnummer;
    private Integer foreslagenSjukskrivningstid;

    public MaximalSjukskrivningstidRequest() {
    }

    private MaximalSjukskrivningstidRequest(
            final Icd10KoderRequest icd10Koder,
            final Personnummer personnummer,
            final Integer foreslagenSjukskrivningstid) {
        this.icd10Koder = icd10Koder;
        this.personnummer = personnummer;
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
    }

    public Icd10KoderRequest getIcd10Koder() {
        return icd10Koder;
    }

    public void setIcd10Koder(final Icd10KoderRequest icd10Koder) {
        this.icd10Koder = icd10Koder;
    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(final Personnummer personnummer) {
        this.personnummer = personnummer;
    }

    public Integer getForeslagenSjukskrivningstid() {
        return foreslagenSjukskrivningstid;
    }

    public void setForeslagenSjukskrivningstid(final Integer foreslagenSjukskrivningstid) {
        this.foreslagenSjukskrivningstid = foreslagenSjukskrivningstid;
    }

    public static MaximalSjukskrivningstidRequest of(
            final Icd10KoderRequest icd10Koder,
            final Personnummer personnummer,
            final Integer foreslagenSjukskrivningstid) {
        return new MaximalSjukskrivningstidRequest(icd10Koder, personnummer, foreslagenSjukskrivningstid);
    }
}
