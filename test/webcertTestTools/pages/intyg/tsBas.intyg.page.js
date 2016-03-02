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

/**
 * Created by bennysce on 09/06/15.
 */
'use strict';

var BaseIntyg = require('./base.intyg.page.js');

var TsBasIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-bas';
        this.vansterOgautanKorrektion = element(by.id('vansterOgautanKorrektion'));
        this.hogerOgautanKorrektion = element(by.id('hogerOgautanKorrektion'));
        this.vansterOgamedKorrektion = element(by.id('vansterOgamedKorrektion'));
        this.binokulartutanKorrektion = element(by.id('binokulartutanKorrektion'));
        this.binokulartmedKorrektion = element(by.id('binokulartmedKorrektion'));
        this.korrektionsglasensStyrka = element(by.id('korrektionsglasensStyrka'));
        this.hogerOgamedKorrektion = element(by.id('hogerOgamedKorrektion'));
        this.horselBalansbalansrubbningar = element(by.id('horselBalansbalansrubbningar'));
        this.funktionsnedsattning = element(by.id('funktionsnedsattning'));
        this.funktionsnedsattningbeskrivning = element(by.id('funktionsnedsattningbeskrivning'));

        this.funktionsnedsRorelseformaga = element(by.id('funktionsnedsattningotillrackligRorelseformaga'));
        this.hjartKarlSjukdom = element(by.id('hjartKarlSjukdom'));
        this.hjarnskadaEfterTrauma = element(by.id('hjarnskadaEfterTrauma'));
        this.riskfaktorerStroke = element(by.id('riskfaktorerStroke'));
        this.beskrivningRiskfaktorer = element(by.id('beskrivningRiskfaktorer'));

        this.synfaltsdefekter = element(by.id('synfaltsdefekter'));
        this.nattblindhet = element(by.id('nattblindhet'));
        this.diplopi = element(by.id('diplopi'));
        this.nystagmus = element(by.id('nystagmus'));
        this.progressivOgonsjukdom = element(by.id('progressivOgonsjukdom'));

        this.harDiabetes = element(by.id('harDiabetes'));
        this.kost = element(by.id('kost'));
        this.tabletter = element(by.id('tabletter'));
        this.insulin = element(by.id('insulin'));
        this.diabetesTyp = element(by.id('diabetesTyp'));

        this.intygStatus = element(by.id('intyget-sparat-och-ej-komplett-meddelande'));

        this.neurologiskSjukdom = element(by.id('neurologiskSjukdom'));

        this.medvetandestorning = element(by.id('medvetandestorning'));
        this.medvetandestorningbeskrivning = element(by.id('medvetandestorningbeskrivning'));
        this.nedsattNjurfunktion = element(by.id('nedsattNjurfunktion'));

        this.sviktandeKognitivFunktion = element(by.id('sviktandeKognitivFunktion'));
        this.teckenSomnstorningar = element(by.id('teckenSomnstorningar'));
        this.teckenMissbruk = element(by.id('teckenMissbruk'));
        this.foremalForVardinsats = element(by.id('foremalForVardinsats'));
        this.provtagningBehovs = element(by.id('provtagningBehovs'));
        this.lakarordineratLakemedelsbruk = element(by.id('lakarordineratLakemedelsbruk'));

        this.lakemedelOchDos = element(by.id('lakemedelOchDos'));
        this.psykiskSjukdom = element(by.id('psykiskSjukdom'));

        this.psykiskUtvecklingsstorning = element(by.id('psykiskUtvecklingsstorning'));
        this.harSyndrom = element(by.id('harSyndrom'));

        this.sjukhusEllerLakarkontakt = element(by.id('sjukhusEllerLakarkontakt'));
        this.tidpunkt = element(by.id('tidpunkt'));
        this.vardinrattning = element(by.id('vardinrattning'));
        this.sjukhusvardanledning = element(by.id('sjukhusvardanledning'));

        this.stadigvarandeMedicinering = element(by.id('stadigvarandeMedicinering'));
        this.medicineringbeskrivning = element(by.id('medicineringbeskrivning'));
        this.intygetAvser = element(by.id('intygAvser'));
        this.idStarktGenom = element(by.id('identitet'));

        this.hogerOgakontaktlins = element(by.id('hogerOgakontaktlins'));
        this.vansterOgakontaktlins = element(by.id('vansterOgakontaktlins'));

    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    }
});

module.exports = new TsBasIntyg();
