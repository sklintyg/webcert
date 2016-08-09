/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.integration.pp.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.riv.infrastructure.directory.privatepractitioner.types.v1.*;
import se.riv.infrastructure.directory.privatepractitioner.v1.*;


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
        l.add(buildBefattning("202010", "Specialistläkare"));
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
