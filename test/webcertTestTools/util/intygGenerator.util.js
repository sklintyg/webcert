/**
 * Created by BESA on 2015-11-23.
 */
'use strict';
var fkIntyg = require('./../testdata/intyg.fk7263.json');

/*
 Options
 {
 personnr : '',
 patientNamn : '',
 issuerId : '',
 issuer : '',
 issued : '',
 validFrom : '',
 validTo : '',
 enhetId : '',
 enhet : '',
 vardgivarId : '',
 intygType : '',
 intygId : '',
 idTemplate : '',
 mall : '',
 from : '',
 to : '',
 sent : '',
 revoked : '',
 deletedByCareGiver : '',
 responseStatus : '',
 template : ''
 }
 */
function _buildIntyg(intygOptions) {

    var jsonDocument = buildDocumentFromIntygTemplate(fkIntyg, intygOptions);

    var stateList = [{state:'RECEIVED', target:'MI', timestamp: intygOptions.issued + 'T12:00:00.000'}];
    if (intygOptions.sent) {
        stateList.push({state:'SENT', target:'FK', timestamp: intygOptions.issued + 'T12:00:10.000'});
    }
    if (intygOptions.revoked) {
        stateList.push({state:'CANCELLED', target:'MI', timestamp: intygOptions.issued + 'T13:00:00.000'});
    }

    var additionalInfo = '';
    if (intygOptions.intygType === 'fk7263') {
        additionalInfo = intygOptions.validFrom + ' - ' + intygOptions.validTo;
/*  }  else if (intygType.equalsIgnoreCase('ts-bas') || intygType.equalsIgnoreCase('ts-diabetes')) {
        def korkortstyper = certificate.intygAvser.korkortstyp*.type
        additionalInfo = '${korkortstyper.join(', ')}'*/
    }

/*    reset() {
        mall = "M"
        utfärdarId = "EttUtfärdarId"
        utfärdare = "EnUtfärdare"
        enhetsId = "1.2.3"
        giltigtFrån = null
        giltigtTill = null
        template = null
        skickat = false
        rättat = false
        deletedByCareGiver = false
    }
  */
    return {
        id: intygOptions.intygId, // id, issued??
        type: intygOptions.intygType,
        civicRegistrationNumber: intygOptions.personnr,
        signedDate: intygOptions.issued,
        signingDoctorName: intygOptions.issuer,
        validFromDate: intygOptions.validFrom,
        validToDate: intygOptions.validTo,
        careUnitId: (intygOptions.enhetId) ? intygOptions.enhetId : '1.2.3',
        careUnitName: intygOptions.enhet ? intygOptions.enhet : 'Enheten',
        careGiverId: intygOptions.vardgivarId ? intygOptions.vardgivarId : '4.5.6',
        deletedByCareGiver: typeof intygOptions.deletedByCareGiver === 'undefined' ? false : true,
        additionalInfo: additionalInfo,
        certificateStates: stateList,
        document: jsonDocument
    };
}

function buildDocumentFromIntygTemplate(intyg, intygOptions) {

    // setting the certificate ID
    intyg.id = intygOptions.intygId;

    // setting personnr in certificate XML
    intyg.grundData.patient.personId = intygOptions.personnr;

    // Ange patientens namn
    if (intygOptions.patientNamn) {
        intyg.grundData.patient.fullstandigtNamn = intygOptions.patientNamn;
    }
    if (intygOptions.issuerId) {
        intyg.grundData.skapadAv.personId = intygOptions.issuerId;
    }
    if (intygOptions.issuer) {
        intyg.grundData.skapadAv.fullstandigtNamn = intygOptions.issuer;
    }
    if (intygOptions.enhetId) {
        intyg.grundData.skapadAv.vardenhet.enhetsid = intygOptions.enhetId;
    }
    if (intygOptions.enhet) {
        intyg.grundData.skapadAv.vardenhet.enhetsnamn = intygOptions.enhet;
    }
    if (intygOptions.vardgivarId) {
        intyg.grundData.skapadAv.vardenhet.vardgivare.vardgivarid = intygOptions.vardgivarId;
    }

    // setting the signing date, from date and to date
    intyg.grundData.signeringsdatum = intygOptions.issued;
    if (intygOptions.intygType === 'fk7263') {
        overrideFkDefaults(intyg, intygOptions);
    }

    return JSON.stringify(intyg);
}

function overrideFkDefaults(intyg, intygOptions) {
    var issuedDate = intygOptions.issued;
    if (intyg.undersokningAvPatienten) {
        intyg.undersokningAvPatienten = issuedDate;
    }
    if (intyg.telefonkontaktMedPatienten) {
        intyg.telefonkontaktMedPatienten = issuedDate;
    }
    if (intyg.journaluppgifter) {
        intyg.journaluppgifter = issuedDate;
    }
    if (intyg.annanReferens) {
        intyg.annanReferens = issuedDate;
    }
    if (intyg.nedsattMed100) {
        if (!intyg.nedsattMed100.from) {
            intyg.nedsattMed100.from = intygOptions.validFrom;
        }
        if (!intyg.nedsattMed100.tom) {
            intyg.nedsattMed100.tom = intygOptions.validTo;
        }
    }
    if (intyg.nedsattMed75) {
        if (!intyg.nedsattMed75.from) {
            intyg.nedsattMed75.from = intygOptions.validFrom;
        }
        if (!intyg.nedsattMed75.tom) {
            intyg.nedsattMed75.tom = intygOptions.validTo;
        }
    }
    if (intyg.nedsattMed50) {
        if (!intyg.nedsattMed50.from) {
            intyg.nedsattMed50.from = intygOptions.validFrom;
        }
        if (!intyg.nedsattMed50.tom) {
            intyg.nedsattMed50.tom = intygOptions.validTo;
        }
    }
    if (intyg.nedsattMed25) {
        if (!intyg.nedsattMed25.from) {
            intyg.nedsattMed25.from = intygOptions.validFrom;
        }
        if (!intyg.nedsattMed25.tom) {
            intyg.nedsattMed25.tom = intygOptions.validTo;
        }
    }
    intyg.giltighet.from = intygOptions.validFrom;
    intyg.giltighet.tom = intygOptions.validTo;
}

module.exports = {
    buildIntyg : _buildIntyg
};