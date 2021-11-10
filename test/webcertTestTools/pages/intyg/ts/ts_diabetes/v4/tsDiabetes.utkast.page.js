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
/*globals element,by, Promise*/
'use strict';

var BaseTsUtkast = require('../../ts.base.utkast.page.js');
var pageHelpers = require('../../../../pageHelper.util.js');
var testTools = require('common-testtools');
testTools.protractorHelpers.init();

var TsDiabetesUtkast = BaseTsUtkast._extend({
  init: function init() {
    init._super.call(this);
    this.intygType = 'ts-diabetes';
    this.intygTypeVersion = '4.0';
    this.at = element(by.id('edit-ts-diabetes'));

    this.intygetAvser = element(by.id('form_intygAvser-kategorier')).all(by.css('label'));
    this.identitet = element(by.id('form_identitetStyrktGenom-typ'));

    this.allmant = {
      patientenFoljsAv: element(by.id('form_allmant-patientenFoljsAv')),
      diabetesDiagnosAr: element(by.id('allmant-diabetesDiagnosAr')),
      typAvDiabetes: element(by.id('form_allmant-typAvDiabetes')),
      beskrivningAnnanTyp: element(by.id('allmant-beskrivningAnnanTypAvDiabetes')),
      medicinering: {
        yes: element(by.id('allmant-medicineringForDiabetesYes')),
        no: element(by.id('allmant-medicineringForDiabetesNo'))
      },
      medicineringHypoglykemi: {
        yes: element(by.id('allmant-medicineringMedforRiskForHypoglykemiYes')),
        no: element(by.id('allmant-medicineringMedforRiskForHypoglykemiNo'))
      },
      behandling: {
        insulin: element(by.id('allmant-behandling-insulin')),
        tabletter: element(by.id('allmant-behandling-tabletter')),
        annan: element(by.id('allmant-behandling-annan')),
        vilkenAnnanBehandling: element(by.id('allmant-behandling-annanAngeVilken'))
      },
      medicineringHypoglykemiTidpunkt: element(by.id('datepicker_allmant.medicineringMedforRiskForHypoglykemiTidpunkt'))
    };

    this.hypoglykemi = {
      kontrollSjukdomstillstand: {
        yes: element(by.id('hypoglykemi-kontrollSjukdomstillstandYes')),
        no: element(by.id('hypoglykemi-kontrollSjukdomstillstandNo'))
      },
      kontrollSjukdomstillstandVarfor: element(by.id('hypoglykemi-kontrollSjukdomstillstandVarfor')),
      forstarRisker: {
        yes: element(by.id('hypoglykemi-forstarRiskerMedHypoglykemiYes')),
        no: element(by.id('hypoglykemi-forstarRiskerMedHypoglykemiNo'))
      },
      kannaVarningstecken: {
        yes: element(by.id('hypoglykemi-formagaKannaVarningsteckenYes')),
        no: element(by.id('hypoglykemi-formagaKannaVarningsteckenNo'))
      },
      adekvataAtgarder: {
        yes: element(by.id('hypoglykemi-vidtaAdekvataAtgarderYes')),
        no: element(by.id('hypoglykemi-vidtaAdekvataAtgarderNo'))
      },
      aterkommandeSenasteTolv: {
        yes: element(by.id('hypoglykemi-aterkommandeSenasteAretYes')),
        no: element(by.id('hypoglykemi-aterkommandeSenasteAretNo'))
      },
      aterkommandeSenasteTolvTidpunkt: element(by.id('datepicker_hypoglykemi.aterkommandeSenasteAretTidpunkt')),
      kontrollerasRegelbundet: {
        yes: element(by.id('hypoglykemi-aterkommandeSenasteAretKontrollerasYes')),
        no: element(by.id('hypoglykemi-aterkommandeSenasteAretKontrollerasNo'))
      },
      trafiksakerhetsrisk: {
        yes: element(by.id('hypoglykemi-aterkommandeSenasteAretTrafikYes')),
        no: element(by.id('hypoglykemi-aterkommandeSenasteAretTrafikNo'))
      },
      senasteTolvVaket: {
        yes: element(by.id('hypoglykemi-aterkommandeVaketSenasteTolvYes')),
        no: element(by.id('hypoglykemi-aterkommandeVaketSenasteTolvNo'))
      },
      senasteTre: {
        yes: element(by.id('hypoglykemi-aterkommandeVaketSenasteTreYes')),
        no: element(by.id('hypoglykemi-aterkommandeVaketSenasteTreNo'))
      },
      senasteTreTidpunkt: element(by.id('datepicker_hypoglykemi.aterkommandeVaketSenasteTreTidpunkt')),
      allvarligSenasteTolv: {
        yes: element(by.id('hypoglykemi-allvarligSenasteTolvManadernaYes')),
        no: element(by.id('hypoglykemi-allvarligSenasteTolvManadernaNo'))
      },
      allvarligSenasteTolvTidpunkt: element(by.id('datepicker_hypoglykemi.allvarligSenasteTolvManadernaTidpunkt')),
      blodsockerkontroller: {
        yes: element(by.id('hypoglykemi-regelbundnaBlodsockerkontrollerYes')),
        no: element(by.id('hypoglykemi-regelbundnaBlodsockerkontrollerNo'))
      }
    };

    this.ovrigt = {
      komplikationer: {
        yes: element(by.id('ovrigt-komplikationerAvSjukdomenYes')),
        no: element(by.id('ovrigt-komplikationerAvSjukdomenNo'))
      },
      komplikationerVilka: element(by.id('ovrigt-komplikationerAvSjukdomenAnges')),
      undersokasSpecialistkompetens: element(by.id('ovrigt-borUndersokasAvSpecialist'))
    };

    this.bedomning = {
      uppfyllerKrav: element(by.id('form_bedomning-uppfyllerBehorighetskrav')).all(by.css('label')),
      ovrigaKommentarer: element(by.id('bedomning-ovrigaKommentarer'))
    };

  },

  fillIntygetAvser: function(intygetAvserKategorier) {
    return pageHelpers.selectAllCheckBoxes(this.intygetAvser, intygetAvserKategorier);
  },

  fillIdentitetStyrktGenom: function(typeOfId) {
    return this.identitet.element(by.cssContainingText('label', typeOfId)).click();
  },

  fillAllmant: function(allmant) {
    var promisesArr = [];
    promisesArr.push(this.allmant.patientenFoljsAv.element(by.cssContainingText('label', allmant.patientenFoljsAv)).click());
    promisesArr.push(this.allmant.diabetesDiagnosAr.typeKeys(allmant.diabetesDiagnosAr));
    promisesArr.push.apply(this.getTypAvDiabetes(allmant));
    promisesArr.push.apply(this.getMedicinering(allmant));
    promisesArr.push.apply(this.getBehandling(allmant));
    return Promise.all(promisesArr);
  },

  fillHypoglykemiForMedication: function(hypoglykemi) {
    var promisesArr = [];
    promisesArr.push.apply(this.getKontrollSjukdomstillstand(hypoglykemi));
    promisesArr.push(hypoglykemi.forstarRisker === 'Ja' ?
        this.hypoglykemi.forstarRisker.yes.click() : this.hypoglykemi.forstarRisker.no.click());
    promisesArr.push(hypoglykemi.kannaVarningstecken === 'Ja' ?
        this.hypoglykemi.kannaVarningstecken.yes.click() : this.hypoglykemi.kannaVarningstecken.no.click());
    promisesArr.push(hypoglykemi.adekvataAtgarder === 'Ja' ?
        this.hypoglykemi.adekvataAtgarder.yes.click() : this.hypoglykemi.adekvataAtgarder.no.click());
    promisesArr.push.apply(this.getAterkommandeSenasteTolv(hypoglykemi));
    promisesArr.push.apply(this.getAterkommandeVaketSenasteTolv(hypoglykemi));

    return Promise.all(promisesArr);
  },

  fillHypoglykemiForHogreBehorigheter: function(hypoglykemi) {
    var promisesArr = [];
    promisesArr.push.apply(this.getAllvarligSenastetolv(hypoglykemi));
    promisesArr.push(hypoglykemi.blodsockerkontroller === 'Ja' ?
        this.hypoglykemi.blodsockerkontroller.yes.click() : this.hypoglykemi.blodsockerkontroller.no.click());

    return Promise.all(promisesArr);
  },

  fillOvrigt: function(ovrigt) {
    var promisesArr = [];
    promisesArr.push.apply(this.getKomplikationer(ovrigt));
    this.getUndersokasSpecialist(ovrigt, promisesArr);
    return Promise.all(promisesArr);
  },

  fillBedomning: function(bedomning) {
    var promisesArr = [];
    promisesArr.push(pageHelpers.selectAllCheckBoxes(this.bedomning.uppfyllerKrav, bedomning.uppfyllerKrav));
    this.enableAutosave();
    this.getOvrigaKommentarer(bedomning, promisesArr);
    return Promise.all(promisesArr);
  },

  getTypAvDiabetes: function(allmant) {
    var typeOfDiabetes = [];
    typeOfDiabetes.push(this.allmant.typAvDiabetes.element(by.cssContainingText('label', allmant.typAvDiabetes)).click());
    if (allmant.typAvDiabetes === 'Annan') {
      typeOfDiabetes.push(this.allmant.beskrivningAnnanTyp.typeKeys(allmant.beskrivningAnnanTyp));
    }
    return typeOfDiabetes;
  },

  getMedicinering: function(allmant) {
    var medication = [];
    medication.push(allmant.medicinering === 'Ja' ? this.allmant.medicinering.yes.click() : this.allmant.medicinering.no.click());
    if (allmant.medicinering === 'Ja') {
      medication.push(allmant.medicineringHypoglykemi === 'Ja' ? this.allmant.medicineringHypoglykemi.yes.click() :
          this.allmant.medicineringHypoglykemi.no.click());
    }
    if (allmant.medicinering === 'Ja' && allmant.medicineringHypoglykemi === 'Ja') {
      medication.push(this.allmant.medicineringHypoglykemiTidpunkt.typeKeys(allmant.medicineringHypoglykemiTidpunkt));
    }
    return medication;
  },

  getBehandling: function(allmant) {
    var treatment = [];
    if (allmant.medicinering === 'Ja' && allmant.medicineringHypoglykemi === 'Ja') {
      allmant.behandling.forEach(function(behandling) {
        if (behandling === 'Insulin') {
          treatment.push(this.allmant.behandling.insulin.click());
        }
        if (behandling === 'Tabletter') {
          treatment.push(this.allmant.behandling.tabletter.click());
        }
        if (behandling === 'Annan') {
          treatment.push(this.allmant.behandling.annan.click());
        }
      }.bind(this));

      if (allmant.medicinering === 'Ja' && allmant.medicineringHypoglykemi === 'Ja' && allmant.behandling.indexOf('Annan') > 0) {
        treatment.push(this.allmant.behandling.vilkenAnnanBehandling.typeKeys(allmant.vilkenAnnanBehandling));
      }
      return treatment;
    }
  },

  getKontrollSjukdomstillstand: function(hypoglykemi) {
    var dieaseStatus = [];
    dieaseStatus.push(hypoglykemi.kontrollSjukdomstillstand === 'Ja' ?
        this.hypoglykemi.kontrollSjukdomstillstand.yes.click() : this.hypoglykemi.kontrollSjukdomstillstand.no.click());
    if (hypoglykemi.kontrollSjukdomstillstand === 'Nej') {
      dieaseStatus.push(this.hypoglykemi.kontrollSjukdomstillstandVarfor.typeKeys(hypoglykemi.kontrollSjukdomstillstandVarfor));
    }
    return dieaseStatus;
  },

  getAterkommandeSenasteTolv: function(hypoglykemi) {
    var recurringLatestTwelve = [];
    recurringLatestTwelve.push(hypoglykemi.aterkommandeSenasteTolv === 'Ja' ?
        this.hypoglykemi.aterkommandeSenasteTolv.yes.click() : this.hypoglykemi.aterkommandeSenasteTolv.no.click());
    if (hypoglykemi.aterkommandeSenasteTolv === 'Ja') {
      recurringLatestTwelve.push(this.hypoglykemi.aterkommandeSenasteTolvTidpunkt.typeKeys(hypoglykemi.aterkommandeSenasteTolvTidpunkt));
      recurringLatestTwelve.push(hypoglykemi.kontrollerasRegelbundet === 'Ja' ?
          this.hypoglykemi.kontrollerasRegelbundet.yes.click() : this.hypoglykemi.kontrollerasRegelbundet.no.click());
      recurringLatestTwelve.push(hypoglykemi.trafiksakerhetsrisk === 'Ja' ?
          this.hypoglykemi.trafiksakerhetsrisk.yes.click() : this.hypoglykemi.trafiksakerhetsrisk.no.click());
    }
    return recurringLatestTwelve;
  },

  getAterkommandeVaketSenasteTolv: function(hypoglykemi) {
    var recurringAwake = [];
    recurringAwake.push(hypoglykemi.senasteTolvVaket === 'Ja' ?
        this.hypoglykemi.senasteTolvVaket.yes.click() : this.hypoglykemi.senasteTolvVaket.no.click());

    if (hypoglykemi.senasteTolvVaket === 'Ja') {
      recurringAwake.push(hypoglykemi.senasteTre === 'Ja' ?
          this.hypoglykemi.senasteTre.yes.click() : this.hypoglykemi.senasteTre.yes.click());
    }

    if (hypoglykemi.senasteTolvVaket === 'Ja' && hypoglykemi.senasteTre) {
      recurringAwake.push(this.hypoglykemi.senasteTreTidpunkt.typeKeys(hypoglykemi.senasteTreTidpunkt));
    }
    return recurringAwake;
  },

  getAllvarligSenastetolv: function(hypoglykemi) {
    var seriousLastTwelve = [];
    seriousLastTwelve.push(hypoglykemi.allvarligSenasteTolv === 'Ja' ?
        this.hypoglykemi.allvarligSenasteTolv.yes.click() : this.hypoglykemi.allvarligSenasteTolv.no.click());
    if (hypoglykemi.allvarligSenasteTolv === 'Ja') {
      seriousLastTwelve.push(this.hypoglykemi.allvarligSenasteTolvTidpunkt.typeKeys(hypoglykemi.allvarligSenasteTolvTidpunkt));
    }
    return seriousLastTwelve;
  },

  getKomplikationer: function(ovrigt) {
    var complications = [];
    complications.push(ovrigt.komplikationer === 'Ja' ?
        this.ovrigt.komplikationer.yes.click() : this.ovrigt.komplikationer.no.click());
    if (ovrigt.komplikationer === 'Ja') {
      complications.push(this.ovrigt.komplikationerVilka.typeKeys(ovrigt.komplikationerVilka));
    }
    return complications;
  },

  getUndersokasSpecialist: function(ovrigt, promisesArr) {
    if (ovrigt.undersokasSpecialistkompetens) {
      promisesArr.push(this.ovrigt.undersokasSpecialistkompetens.typeKeys(ovrigt.undersokasSpecialistkompetens));
    }
  },

  getOvrigaKommentarer: function(bedomning, promisesArr) {
    if (bedomning.ovrigaKommentarer) {
      promisesArr.push(this.bedomning.ovrigaKommentarer.typeKeys(bedomning.ovrigaKommentarer));
    }
  }
});

module.exports = new TsDiabetesUtkast();
