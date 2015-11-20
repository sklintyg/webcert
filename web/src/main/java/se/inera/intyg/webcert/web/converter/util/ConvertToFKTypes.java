package se.inera.intyg.webcert.web.converter.util;

import org.joda.time.LocalDateTime;

import se.inera.certificate.validate.PersonnummerValidator;
import se.inera.certificate.validate.SamordningsnummerValidator;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import iso.v21090.dt.v1.II;

/**
 * Created by pehr on 10/2/13.
 */
public final class ConvertToFKTypes {

    private ConvertToFKTypes() {
    }

    private static final String VARDGIVARE_ROOT = "1.2.752.129.2.1.4.1";
    private static final String ARBETSPLATSKOD_ROOT = "1.2.752.29.4.71";
    private static final String HSAID_ROOT = "1.2.752.129.2.1.4.1";

    public static II toII(String root, String ext) {
        if ((root == null) || (ext == null)) {
            return null;
        }
        II ii = new II();
        ii.setRoot(root);
        ii.setExtension(ext);
        return ii;
    }

    public static Amnetyp toAmneTyp(Amne amne) {
        switch (amne) {
        case ARBETSTIDSFORLAGGNING:
            return Amnetyp.ARBETSTIDSFORLAGGNING;
        case AVSTAMNINGSMOTE:
            return Amnetyp.AVSTAMNINGSMOTE;
        case KOMPLETTERING_AV_LAKARINTYG:
            return Amnetyp.KOMPLETTERING_AV_LAKARINTYG;
        case KONTAKT:
            return Amnetyp.KONTAKT;
        case MAKULERING_AV_LAKARINTYG:
            return Amnetyp.MAKULERING_AV_LAKARINTYG;
        case OVRIGT:
            return Amnetyp.OVRIGT;
        case PAMINNELSE:
            return Amnetyp.PAMINNELSE;
        default:
            return null;
        }

    }

    public static InnehallType toInnehallType(String text, LocalDateTime singeringsDatum) {
        InnehallType iht = new InnehallType();
        iht.setMeddelandeText(text);
        if (singeringsDatum != null) {
            iht.setSigneringsTidpunkt(singeringsDatum);
        }
        return iht;
    }

    public static LakarutlatandeEnkelType toLakarUtlatande(IntygsReferens ir) {
        if (ir == null) {
            return null;
        }

        LakarutlatandeEnkelType lu = new LakarutlatandeEnkelType();
        lu.setLakarutlatandeId(ir.getIntygsId());

        PatientType pt = new PatientType();
        pt.setFullstandigtNamn(ir.getPatientNamn());

        String root = PersonnummerValidator.PERSONNUMMER_ROOT;
        if (ir.getPatientId().isSamordningsNummer()) {
            root = SamordningsnummerValidator.SAMORDNINGSNUMMER_ROOT;
        }

        pt.setPersonId(toII(root, ir.getPatientId().getPersonnummer()));

        lu.setPatient(pt);
        lu.setSigneringsTidpunkt(ir.getSigneringsDatum());

        return lu;
    }

    public static VardAdresseringsType toVardAdresseringsType(Vardperson vp) {
        if (vp == null) {
            return null;
        }

        VardAdresseringsType vat = new VardAdresseringsType();

        HosPersonalType hos = new HosPersonalType();
        hos.setForskrivarkod(vp.getForskrivarKod());
        hos.setFullstandigtNamn(vp.getNamn());
        hos.setPersonalId(toII(HSAID_ROOT, vp.getHsaId()));

        hos.setEnhet(toEnhetType(vp));

        vat.setHosPersonal(hos);

        return vat;
    }

    public static EnhetType toEnhetType(Vardperson vp) {
        if (vp == null) {
            return null;
        }
        EnhetType et = new EnhetType();
        if (vp.getEnhetsId() != null) {
            et.setEnhetsId(toII(HSAID_ROOT, vp.getEnhetsId()));
        }

        et.setEnhetsnamn(vp.getEnhetsnamn());
        et.setEpost(vp.getEpost());
        et.setPostadress(vp.getPostadress());
        et.setPostnummer(vp.getPostnummer());
        et.setPostort(vp.getPostort());
        et.setTelefonnummer(vp.getTelefonnummer());

        if (vp.getArbetsplatsKod() != null) {
            et.setArbetsplatskod(toII(ARBETSPLATSKOD_ROOT, vp.getArbetsplatsKod()));
        }

        VardgivareType vgt = new VardgivareType();
        if (vp.getVardgivarnamn() != null) {
            vgt.setVardgivarnamn(vp.getVardgivarnamn());
        }

        if (vp.getVardgivarId() != null) {
            vgt.setVardgivareId(toII(VARDGIVARE_ROOT, vp.getVardgivarId()));
        }

        et.setVardgivare(vgt);

        return et;
    }

}
