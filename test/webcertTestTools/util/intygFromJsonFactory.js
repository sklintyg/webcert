/**
 * Created by eriklupander on 2016-04-29.
 */

/*globals JSON*/
'use strict';

// These are pushed to intygstjänsten using restDataHelper.createIntyg
var templateJsonObjLuaefs = require('webcert-testtools/testdata/luae_fs-minimal.json');
var templateJsonObjLuaena = require('webcert-testtools/testdata/intyg.luae_na.json');

// These are pushed to webcert using restDataHelper.createWebcertIntyg
var templateJsonObjWCLuse = require('webcert-testtools/testdata/webcertIntyg.luse.json');

module.exports = {
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
            certificateStates: [
                {
                    target: 'HV',
                    state: 'RECEIVED',
                    timestamp: '2016-04-28T14:00:00.000'
                }
            ],
            revoked: false
        };
    },
    defaultLuaena: function() {
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
            certificateStates: [
                {
                    target: 'HV',
                    state: 'RECEIVED',
                    timestamp: '2016-04-28T14:00:00.000'
                }
            ],
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
