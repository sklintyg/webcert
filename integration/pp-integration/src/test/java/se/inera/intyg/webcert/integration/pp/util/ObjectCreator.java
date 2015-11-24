package se.inera.intyg.webcert.integration.pp.util;

import org.joda.time.LocalDateTime;
import se.inera.intyg.webcert.common.common.hsa.HSABefattning;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.ArbetsplatsKod;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.BefattningType;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.GeografiskIndelningType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.LegitimeradYrkesgruppType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VerksamhetType;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class ObjectCreator {

    public static void main(String[] args) throws Exception {
        ObjectCreator oc = new ObjectCreator();
        ObjectToJson objectToJson = new ObjectToJson(oc.buildHoSPersonType());
        System.out.println(objectToJson.printJson());
    }

    public HoSPersonType getHoSPersonType() {
        return buildHoSPersonType();
    }

    private HoSPersonType buildHoSPersonType() {

        HoSPersonType personType = new HoSPersonType();

        personType.setHsaId(buildHsaId("1.2.752.129.2.1.4.1", "HSA0000-123456789"));
        personType.setEnhet(buildEnhet());
        personType.setForskrivarkod("00000");
        personType.setFullstandigtNamn("Tolvan Tolvansson");
        personType.setGodkandAnvandare(true);
        personType.setPersonId(buildPersonId());
        personType.getBefattning().addAll(buildBefattningar());
        personType.getLegitimeradYrkesgrupp().addAll(buildLegitimeradYrkesgrupper());
        personType.getSpecialitet().addAll(buildSpecialiteter());

        return personType;
    }

    private List<SpecialitetType> buildSpecialiteter() {
        List<SpecialitetType> l = new ArrayList<>();
        l.add(buildSpecialitet("100", "Kirurgi"));
        l.add(buildSpecialitet("200", "Ortopedi"));
        return l;
    }

    private SpecialitetType buildSpecialitet(String kod, String namn) {
        SpecialitetType specialitet = new SpecialitetType();
        specialitet.setKod(kod);
        specialitet.setNamn(namn);
        return specialitet;
    }

    private LegitimeradYrkesgruppType buildLegitimeradYrkesgrupp(String kod, String namn) {
        LegitimeradYrkesgruppType legitimeradYrkesgrupp = new LegitimeradYrkesgruppType();
        legitimeradYrkesgrupp.setKod(kod);
        legitimeradYrkesgrupp.setNamn(namn);
        return legitimeradYrkesgrupp;
    }

    private List<LegitimeradYrkesgruppType> buildLegitimeradYrkesgrupper() {
        List<LegitimeradYrkesgruppType> l = new ArrayList<>();
        l.add(buildLegitimeradYrkesgrupp("100", "Läkare"));
        return l;
    }

    private List<BefattningType> buildBefattningar() {
        List<BefattningType> l = new ArrayList<>();
        l.add(buildBefattning(HSABefattning.SPECIALISTLAKARE.getCode(), HSABefattning.SPECIALISTLAKARE.getDescription()));
        return l;
    }

    private BefattningType buildBefattning(String kod, String namn) {
        BefattningType befattning = new BefattningType();
        befattning.setKod(kod);
        befattning.setNamn(namn);
        return befattning;
    }

    private PersonId buildPersonId() {
        PersonId personId = new PersonId();
        personId.setRoot("1.2.752.129.2.1.3.1");
        personId.setExtension("19121212-1212");
        return personId;
    }

    private EnhetType buildEnhet() {
        EnhetType enhetType = new EnhetType();

        enhetType.setAgarform("Privat");
        enhetType.setArbetsplatskod(buildArbetsplatskod());
        enhetType.setEnhetsId(buildHsaId("1.2.752.129.2.1.4.1", "HSA123456789-0000"));
        enhetType.setEnhetsnamn("Privatenheten");
        enhetType.setEpost("privat@privatenhet.pe");
        enhetType.setGeografiskIndelning(buildGeografiskIndelning());
        enhetType.setPostadress("Privatgatan 1");
        enhetType.setPostnummer("12345");
        enhetType.setPostort("Privatstan");
        enhetType.setStartdatum(new LocalDateTime(2014, 05, 15, 7, 0));
        enhetType.setSlutdatum(null);
        enhetType.setVardgivare(buildVardgivare());
        enhetType.setVerksamhetstyp(buildVerksamhetstyp());

        return enhetType;
    }

    private HsaId buildHsaId(String root, String extension) {
        HsaId hsaId = new HsaId();
        hsaId.setRoot(root);
        hsaId.setExtension(extension);
        return hsaId;
    }

    private VerksamhetType buildVerksamhetstyp() {
        return new VerksamhetType();
    }

    private VardgivareType buildVardgivare() {
        return new VardgivareType();
    }

    private GeografiskIndelningType buildGeografiskIndelning() {
        return new GeografiskIndelningType();
    }

    private ArbetsplatsKod buildArbetsplatskod() {
        return new ArbetsplatsKod();
    }
}
