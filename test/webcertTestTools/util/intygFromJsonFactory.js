/**
 * Created by eriklupander on 2016-04-29.
 */

/*globals JSON*/
'use strict';

var templateJsonObj = require('webcert-testtools/testdata/luae_fs-minimal.json');

module.exports = {
    defaultLuaefs: function() {

        return {
            id: templateJsonObj.id,
            document: JSON.stringify(templateJsonObj),
            originalCertificate: '',
            type: templateJsonObj.typ,
            signingDoctorName: templateJsonObj.grundData.skapadAv.fullstandigtNamn,
            careUnitId: templateJsonObj.grundData.skapadAv.vardenhet.enhetsid,
            careUnitName: templateJsonObj.grundData.skapadAv.vardenhet.enhetsnamn,
            careGiverId: templateJsonObj.grundData.skapadAv.vardenhet.vardgivare.vardgivarid,
            civicRegistrationNumber: templateJsonObj.grundData.patient.personId,
            signedDate: templateJsonObj.grundData.signeringsdatum,
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HV',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
    }
};