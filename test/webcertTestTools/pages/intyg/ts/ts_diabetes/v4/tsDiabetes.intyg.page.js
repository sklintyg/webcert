/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
/*globals element,by*/

'use strict';
var TsBaseIntyg = require('../../ts.base.intyg.page');

var TsDiabetesIntyg = TsBaseIntyg._extend({
  init: function init() {
    init._super.call(this);
    this.intygType = 'ts-diabetes';
    this.intygTypeVersion = '4.0';

    this.intygetAvserKategorier = element(by.id('intygAvser-kategorier'));
    this.identitetStyrktGenom = element(by.id('identitetStyrktGenom-typ'));

    this.allmant = {
      patientenFoljsAv: element(by.id('allmant-patientenFoljsAv')),
      diabetesDiagnosAr: element(by.id('allmant-diabetesDiagnosAr')),
      typAvDiabetes: element(by.id('allmant-typAvDiabetes')),
      beskrivningAnnanTyp: element(by.id('allmant-beskrivningAnnanTypAvDiabetes')),
      medicinering: element(by.id('allmant-medicineringForDiabetes')),
      medicineringHypoglykemi: element(by.id('allmant-medicineringMedforRiskForHypoglykemi')),
      behandling: element(by.id('["allmant.behandling.insulin","allmant.behandling.tabletter","allmant.behandling.annan"]')),
      vilkenAnnanBehandling: element(by.id('allmant-behandling-annanAngeVilken')),
      medicineringHypoglykemiTidpunkt: element(by.id('allmant-medicineringMedforRiskForHypoglykemiTidpunkt'))
    };

    this.hypoglykemi = {
      kontrollSjukdomstillstand: element(by.id('hypoglykemi-kontrollSjukdomstillstand')),
      kontrollSjukdomstillstandVarfor: element(by.id('hypoglykemi-kontrollSjukdomstillstandVarfor')),
      forstarRisker: element(by.id('hypoglykemi-forstarRiskerMedHypoglykemi')),
      kannaVarningstecken: element(by.id('hypoglykemi-formagaKannaVarningstecken')),
      adekvataAtgarder: element(by.id('hypoglykemi-vidtaAdekvataAtgarder')),
      aterkommandeSenasteTolv: element(by.id('hypoglykemi-aterkommandeSenasteAret')),
      aterkommandeSenasteTolvTidpunkt: element(by.id('hypoglykemi-aterkommandeSenasteAretTidpunkt')),
      kontrollerasRegelbundet: element(by.id('hypoglykemi-aterkommandeSenasteAretKontrolleras')),
      trafiksakerhetsrisk: element(by.id('hypoglykemi-aterkommandeSenasteAretTrafik')),
      senasteTolvVaket: element(by.id('hypoglykemi-aterkommandeVaketSenasteTolv')),
      senasteTre: element(by.id('hypoglykemi-aterkommandeVaketSenasteTre')),
      senasteTreTidpunkt: element(by.id('hypoglykemi-aterkommandeVaketSenasteTreTidpunkt')),
      allvarligSenasteTolv: element(by.id('hypoglykemi-allvarligSenasteTolvManaderna')),
      allvarligSenasteTolvTidpunkt: element(by.id('hypoglykemi-allvarligSenasteTolvManadernaTidpunkt')),
      blodsockerkontroller: element(by.id('hypoglykemi-regelbundnaBlodsockerkontroller'))
    };

    this.ovrigt = {
      komplikationer: element(by.id('ovrigt-komplikationerAvSjukdomen')),
      komplikationerVilka: element(by.id('ovrigt-komplikationerAvSjukdomenAnges')),
      undersokasSpecialistkompetens: element(by.id('ovrigt-borUndersokasAvSpecialist'))
    };

    this.bedomning = {
      uppfyllerKrav: element(by.id('bedomning-uppfyllerBehorighetskrav')),
      ovrigaKommentarer: element(by.id('bedomning-ovrigaKommentarer'))
    };
  },

  verifyIntygetAvser: function(intygetAvserKategorier) {
    var expectedIntygetAvserKategorier = intygetAvserKategorier.sort().join(', ');
    var actualIntygetAvserKategorier = this.intygetAvserKategorier.getText().then(function(text) {
      return text.split(', ').sort().join(', ');
    });

    expect(actualIntygetAvserKategorier).toBe(expectedIntygetAvserKategorier);
  },

  verifyIdentitetStyrktGenom: function(identitetStyrktGenom) {
    expect(this.identitetStyrktGenom.getText()).toBe(identitetStyrktGenom);
  },

  verifyAllmant: function(allmant) {
    expect(this.allmant.patientenFoljsAv.getText()).toBe(allmant.patientenFoljsAv);
    expect(this.allmant.diabetesDiagnosAr.getText()).toBe(allmant.diabetesDiagnosAr);
    expect(this.allmant.typAvDiabetes.getText()).toBe(allmant.typAvDiabetes);
    expect(this.allmant.typAvDiabetes.getText()).toBe(allmant.typAvDiabetes);
    expect(this.allmant.beskrivningAnnanTyp.getText()).toBe(allmant.beskrivningAnnanTyp);
    expect(this.allmant.medicinering.getText()).toBe(allmant.medicinering);
    expect(this.allmant.medicineringHypoglykemi.getText()).toBe(allmant.medicineringHypoglykemi);
    expect(this.allmant.vilkenAnnanBehandling.getText()).toBe(allmant.vilkenAnnanBehandling);
    expect(this.allmant.medicineringHypoglykemiTidpunkt.getText()).toBe(allmant.medicineringHypoglykemiTidpunkt);

    var expectedBehandling = allmant.behandling.sort().join(', ');
    var actualBehandling = this.allmant.behandling.getText().then(function(text) {
      return text.split(', ').sort().join(', ');
    });

    expect(actualBehandling).toBe(expectedBehandling);
  },

  verifyHypoglykemi: function(hypoglykemi) {
    expect(this.hypoglykemi.kontrollSjukdomstillstand.getText()).toBe(hypoglykemi.kontrollSjukdomstillstand);
    expect(this.hypoglykemi.kontrollSjukdomstillstandVarfor.getText()).toBe(hypoglykemi.kontrollSjukdomstillstandVarfor);
    expect(this.hypoglykemi.forstarRisker.getText()).toBe(hypoglykemi.forstarRisker);
    expect(this.hypoglykemi.kannaVarningstecken.getText()).toBe(hypoglykemi.kannaVarningstecken);
    expect(this.hypoglykemi.adekvataAtgarder.getText()).toBe(hypoglykemi.adekvataAtgarder);
    expect(this.hypoglykemi.aterkommandeSenasteTolv.getText()).toBe(hypoglykemi.aterkommandeSenasteTolv);
    expect(this.hypoglykemi.aterkommandeSenasteTolvTidpunkt.getText()).toBe(hypoglykemi.aterkommandeSenasteTolvTidpunkt);
    expect(this.hypoglykemi.kontrollerasRegelbundet.getText()).toBe(hypoglykemi.kontrollerasRegelbundet);
    expect(this.hypoglykemi.trafiksakerhetsrisk.getText()).toBe(hypoglykemi.trafiksakerhetsrisk);
    expect(this.hypoglykemi.senasteTolvVaket.getText()).toBe(hypoglykemi.senasteTolvVaket);
    expect(this.hypoglykemi.senasteTre.getText()).toBe(hypoglykemi.senasteTre);
    expect(this.hypoglykemi.senasteTreTidpunkt.getText()).toBe(hypoglykemi.senasteTreTidpunkt);
    expect(this.hypoglykemi.allvarligSenasteTolv.getText()).toBe(hypoglykemi.allvarligSenasteTolv);
    expect(this.hypoglykemi.allvarligSenasteTolvTidpunkt.getText()).toBe(hypoglykemi.allvarligSenasteTolvTidpunkt);
    expect(this.hypoglykemi.blodsockerkontroller.getText()).toBe(hypoglykemi.blodsockerkontroller);
  },

  verifyOvrigt: function(ovrigt) {
    expect(this.ovrigt.komplikationer.getText()).toBe(ovrigt.komplikationer);
    expect(this.ovrigt.komplikationerVilka.getText()).toBe(ovrigt.komplikationerVilka);
    expect(this.ovrigt.undersokasSpecialistkompetens.getText()).toBe(ovrigt.undersokasSpecialistkompetens);
  },

  verifyBedomning: function(bedomning) {
    var expectedUppfyllerKrav = bedomning.uppfyllerKrav.sort().join(', ');
    var actualUppfyllerKrav = this.bedomning.uppfyllerKrav.getText().then(function(text) {
      return text.split(', ').sort().join(', ');
    });

    expect(actualUppfyllerKrav).toBe(expectedUppfyllerKrav);
    expect(this.bedomning.ovrigaKommentarer.getText()).toBe(bedomning.ovrigaKommentarer);
  },

  verify: function(data) {
    this.verifyIntygetAvser(data.intygetAvserKategorier);
    this.verifyIdentitetStyrktGenom(data.identitetStyrktGenom);
    this.verifyAllmant(data.allmant);
    this.verifyHypoglykemi(data.hypoglykemi);
    this.verifyOvrigt(data.ovrigt);
    this.verifyBedomning(data.bedomning);
  }
});

module.exports = new TsDiabetesIntyg();
