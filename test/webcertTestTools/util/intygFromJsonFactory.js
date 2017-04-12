/**
 * Created by eriklupander on 2016-04-29.
 */

/*globals JSON*/
'use strict';

// These are pushed to intygstjänsten using restDataHelper.createIntyg
var templateJsonObjFK7263 = require('webcert-testtools/testdata/intyg.fk7263.json');
var templateJsonObjLuaefs = require('webcert-testtools/testdata/intyg.luae_fs.minimal.json');
var templateJsonObjLuaena = require('webcert-testtools/testdata/intyg.luae_na.json');
var templateJsonObjLuse = require('webcert-testtools/testdata/intyg.luse.json');
var templateJsonObjLisu = require('webcert-testtools/testdata/intyg.lisu.json');

// These are pushed to webcert using restDataHelper.createWebcertIntyg
var templateJsonObjWCLuse = require('webcert-testtools/testdata/webcertIntyg.luse.json');

module.exports = {
    defaultFK7263: function() {

        templateJsonObjFK7263.id = guid();

        return {
            id: templateJsonObjFK7263.id,
            document: JSON.stringify(templateJsonObjFK7263),
            originalCertificate: '',
            type: templateJsonObjFK7263.typ,
            signingDoctorName: templateJsonObjFK7263.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObjFK7263.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObjFK7263.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObjFK7263.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObjFK7263.grundData.patient.personId,
            signedDate: templateJsonObjFK7263.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HSVARD',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    },
    defaultLuse: function() {

        templateJsonObjLuse.id = guid();

        return {
            id: templateJsonObjLuse.id,
            document: JSON.stringify(templateJsonObjLuse),
            originalCertificate: '',
            type: templateJsonObjLuse.typ,
            signingDoctorName: templateJsonObjLuse.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObjLuse.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObjLuse.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObjLuse.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObjLuse.grundData.patient.personId,
            signedDate: templateJsonObjLuse.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HSVARD',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    },
    defaultLisu: function() {

        templateJsonObjLisu.id = guid();

        return {
            id: templateJsonObjLisu.id,
            document: JSON.stringify(templateJsonObjLisu),
            originalCertificate: '',
            type: templateJsonObjLisu.typ,
            signingDoctorName: templateJsonObjLisu.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObjLisu.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObjLisu.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObjLisu.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObjLisu.grundData.patient.personId,
            signedDate: templateJsonObjLisu.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HSVARD',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    },
    defaultLuaefs: function() {

        templateJsonObjLuaefs.id = guid();

        return {
            id: templateJsonObjLuaefs.id,
            document: JSON.stringify(templateJsonObjLuaefs),
            originalCertificate: '',
            type: templateJsonObjLuaefs.typ,
            signingDoctorName: templateJsonObjLuaefs.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObjLuaefs.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObjLuaefs.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObjLuaefs.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObjLuaefs.grundData.patient.personId,
            signedDate: templateJsonObjLuaefs.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HSVARD',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    },
    defaultLuaena: function() {

        templateJsonObjLuaena.id = guid();

        return {
            id: templateJsonObjLuaena.id,
            document: JSON.stringify(templateJsonObjLuaena),
            originalCertificate: '',
            type: templateJsonObjLuaena.typ,
            signingDoctorName: templateJsonObjLuaena.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObjLuaena.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObjLuaena.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObjLuaena.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObjLuaena.grundData.patient.personId,
            signedDate: templateJsonObjLuaena.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HSVARD',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    },
    defaultWCLuse: function() {
        templateJsonObjWCLuse.contents.id = guid();
        return templateJsonObjWCLuse;
    }
};

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}
