// For comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands

// TODO: Ska dessa konstanter vara deklarerade här eller ska vi ha en
// egen fil med enbart konstanter?
// Dessa konstanter är exakt så som specificerat i
// "Kodverk i nationella tjänsteplattformen", "KV intygstyp"
// https://riv-ta.atlassian.net/wiki/pages/viewpageattachments.action?pageId=270532953
export const implementeradeIntyg = {
    LISJP: "LISJP",
    LUSE: "LUSE",
    LUAE_NA: "LUAE_NA",
    LUAE_FS: "LUAE_FS",
    TS_BAS: "TSTRK1007",
    TS_DIABETES: "TSTRK1031",
    TS_ANMÄLAN: "TSTRK1009",
    TS_ADHD: "TSTRK1062",
    AFMU: "AF00213",
    AF00251: "AF00251",
    DB: "DB"
}

function loggaInVårdpersonal(vårdpersonal, vårdenhet, ärDjup) {
    expect(vårdpersonal).to.exist;
    expect(vårdenhet).to.exist;
    assert.isBoolean(ärDjup);

    const originSträng = (ärDjup ? "DJUPINTEGRATION" : "NORMAL");

    cy.request({
        method: 'POST',
        url: '/fake',
        form: true,
        body: {
            "userJsonDisplay":
                '{"hsaId": "' + vårdpersonal.hsaId + '",\
                "forNamn": "' + vårdpersonal.förnamn + '",\
                "efterNamn": "' + vårdpersonal.efternamn +'",\
                "enhetId": "' + vårdenhet.id + '",\
                "legitimeradeYrkesgrupper": ' + vårdpersonal.legitimeradeYrkesgrupper + ',\
                "origin": "' + originSträng + '",\
                "authenticationMethod": "FAKE"}'
        }
    }).then((resp) => {
        expect(resp.status).to.equal(200);
    });
}

Cypress.Commands.add("loggaInVårdpersonalNormal", (vårdpersonal, vårdenhet) => {
    loggaInVårdpersonal(vårdpersonal, vårdenhet, false);
});

Cypress.Commands.add("loggaInVårdpersonalIntegrerat", (vårdpersonal, vårdenhet) => {
    loggaInVårdpersonal(vårdpersonal, vårdenhet, true);
});

// Skapa ett utkast enligt intygstyp
function skapaUtkast(fx, intygstyp) {
    const vårdpersonal = fx.vårdpersonal;
    const vårdtagare = fx.vårdtagare;
    const vårdenhet = fx.vårdenhet;
    expect(vårdpersonal).to.exist;
    expect(vårdtagare).to.exist;
    expect(vårdenhet).to.exist;

    expect(Object.values(implementeradeIntyg)).to.include.members([intygstyp]);

    cy.request({
        method: 'POST',
        url: '/services/create-draft-certificate/v3.0',
        body:
            '<soapenv:Envelope\
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:3" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3">\
                <soapenv:Header>\
                    <urn:LogicalAddress>?</urn:LogicalAddress>\
                </soapenv:Header>\
                <soapenv:Body>\
                    <urn1:CreateDraftCertificate>\
                        <urn1:intyg>\
                            <urn1:typAvIntyg>\
                                <urn2:code>' + intygstyp + '</urn2:code>\
                                <urn2:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn2:codeSystem>\
                                <urn2:displayName>?</urn2:displayName>\
                            </urn1:typAvIntyg>\
                            <urn1:patient>\
                                <urn3:person-id>\
                                    <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\
                                    <urn2:extension>' + vårdtagare.personnummerKompakt + '</urn2:extension>\
                                </urn3:person-id>\
                                <urn3:fornamn>' + vårdtagare.förnamn + '</urn3:fornamn>\
                                <urn3:efternamn>' + vårdtagare.efternamn + '</urn3:efternamn>\
                                <urn3:postadress>' + vårdtagare.postadress + '</urn3:postadress>\
                                <urn3:postnummer>' + vårdtagare.postnummer + '</urn3:postnummer>\
                                <urn3:postort>' + vårdtagare.postort + '</urn3:postort>\
                            </urn1:patient>\
                            <urn1:skapadAv>\
                                <urn1:personal-id>\
                                    <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\
                                    <urn2:extension>' + vårdpersonal.hsaId + '</urn2:extension>\
                                </urn1:personal-id>\
                                <urn1:fullstandigtNamn>' + vårdpersonal.förnamn + ' ' + vårdpersonal.efternamn + '</urn1:fullstandigtNamn>\
                                <urn1:enhet>\
                                    <urn1:enhets-id>\
                                        <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\
                                        <urn2:extension>' + vårdenhet.id + '</urn2:extension>\
                                    </urn1:enhets-id>\
                                    <urn1:enhetsnamn>' + vårdenhet.namn + '</urn1:enhetsnamn>\
                                </urn1:enhet>\
                            </urn1:skapadAv>\
                        </urn1:intyg>\
                    </urn1:CreateDraftCertificate>\
                </soapenv:Body>\
            </soapenv:Envelope>'
    }).then((resp) => {
        expect(resp.status).to.equal(200);

        cy.wrap(resp).its('body').then((body) => {

            // Check för att konstatera att responsen på tjänsteanropet innehåller resultCode och att värdet är OK
            expect(body).to.contain("resultCode");
            var resultCodeStart = "<ns5:resultCode>"
            var resultCodeEnd = "</ns5:resultCode>"
            var resultCodeStartIdx = body.indexOf(resultCodeStart);
            var resultCodeEndIdx = body.indexOf(resultCodeEnd);
            var resultCode = body.substring(resultCodeStartIdx + resultCodeStart.length, resultCodeEndIdx);
            expect(resultCode).to.equal("OK");
            cy.log(resultCode);

            // Lokalisera intygs-id:t i response body och returnera värdet.
            // Det ligger mellan desa två strängar:
            var substringStart = "ns3:extension";
            var subStringEnd = "</ns3:extension>";

            var subStringStartIndex = body.indexOf(substringStart);
            var subStringEndIndex = body.indexOf(subStringEnd);

            var certificateId = body.substring(subStringStartIndex + substringStart.length + 1, subStringEndIndex);

            // Utan detta klagar Cypress på att man blandar synkron och asynkron kod
            cy.wrap(certificateId).then((id) => {
                return id;
            })
        });
    });
}

// Skapa ett LISJP-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("skapaLisjpUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.LISJP);
});

// Skapa ett LISJP-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("skapaLuseUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.LUSE);
});

// Skapa ett LUAE-NA-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("skapaLuaeNaUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.LUAE_NA);
});

// Skapa ett LUAE-FS-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("skapaLuaeFsUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.LUAE_FS);
});

Cypress.Commands.add("skapaTsBasUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.TS_BAS);
});

Cypress.Commands.add("skapaTsDiabetesUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.TS_DIABETES);
});

Cypress.Commands.add("skapaTsAnmälanUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.TS_ANMÄLAN);
});

Cypress.Commands.add("skapaTsADHDUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.TS_ADHD);
});

Cypress.Commands.add("skapaAFMUUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.AFMU);
});

Cypress.Commands.add("skapaAF00251Utkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.AF00251);
});

Cypress.Commands.add("skapaDBUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.DB);
});

/*
Loopa igenom arrayen och plocka ut antal unika intygsid:n (detta påverkar vilka URL:er
vi ska hämta loggar från). Skapa en tvådimensionell array med intygsid som första
element på varje plats, följt av logghändelserna som ska verifieras.
*/
function delaPdlEventsPåIntygsid(pdlLoggar) {
    var eventPerIntygsid = [];
    for (var i = 0; i < pdlLoggar.length; i++) {
        var nyttId = true
        for (var j = 0; j < eventPerIntygsid.length; j++) {
            if (eventPerIntygsid[j][0] === pdlLoggar[i].activity.activityLevel) {
                nyttId = false
                eventPerIntygsid[j].push(pdlLoggar[i]);
                break;
            }
        }

        if (nyttId) {
            // Vi hittade inte intygsId:t i den nya arrayen vilket betyder att det är ett nytt id som ska läggas till
            var nyttIndex = eventPerIntygsid.length
            eventPerIntygsid[nyttIndex] = []
            eventPerIntygsid[nyttIndex].push(pdlLoggar[i].activity.activityLevel)
            eventPerIntygsid[nyttIndex].push(pdlLoggar[i])
        }
    }

    return eventPerIntygsid;
}

/*
Sorterar en array bestående av PDL-event från mocken i kronologisk ordning
*/
function sorteraHändelserKronologiskt(array) {

    // getElementsByTagName returnerar en HTMLCollection som behöver sorteras. Inspirerat av
    // https://stackoverflow.com/questions/7059090/using-array-prototype-sort-call-to-sort-a-htmlcollection

    expect(array.length).to.be.greaterThan(0);
    array.sort(function(a,b) {
        var datumA = a.getElementsByTagName("startdate")[0].innerText
        var datumB = b.getElementsByTagName("startdate")[0].innerText

        if (datumA < datumB) {
            return -1
        }
        if (datumA > datumB) {
            return 1
        }

        // De båda eventen har samma tidstämpel. Kolla om det gäller samma activity, i så fall kvittar det i vilken ordning
        // de kommer. Om de inte är lika "på djupet" så kommer testfallet ändå att faila längre ner när vi jämför alla fält.
        const activityTypeA = a.getElementsByTagName("activitytype")[0].innerText;
        const activityTypeB = b.getElementsByTagName("activitytype")[0].innerText;

        var activityArgsA = "undefined";
        var activityArgsB = "undefined";
        if (a.getElementsByTagName("activityargs")[0]) {
            activityArgsA = a.getElementsByTagName("activityargs")[0].innerText;
        }
        if (b.getElementsByTagName("activityargs")[0]) {
            activityArgsB = b.getElementsByTagName("activityargs")[0].innerText;
        }

        var ärEventLika = false;
        if ((activityTypeA ===  activityTypeB) &&
            (activityArgsA === activityArgsB)) {
            ärEventLika = true;
        }

        var beskrivningEvents = "Sorteringsalgoritmen hittade två event med samma tidstämpel. "
        beskrivningEvents += "Dessa var " + (ärEventLika ? "" :  "inte ") + "lika. ";
        beskrivningEvents += "A: " + activityTypeA + ", " + activityArgsA + ", " + datumA + ". ";
        beskrivningEvents += "B: " + activityTypeB + ", " + activityArgsB + ", " + datumB + ". ";
        cy.log(beskrivningEvents);

        return 0;
    });
}

/*
Verifiera förväntade händelser mot hämtade (riktiga) händelser för
ett intyg.
*/
function verifieraHändelserFörIntyg(förväntadeHändelser, arr) {
    // Gå igenom listan med logghändelser och bocka av, en efter en.
    // Utgår från https://inera-certificate.atlassian.net/wiki/spaces/IT/pages/41353325/GE-005+PDL-loggning+i+Webcert
    assert.equal(arr.length, förväntadeHändelser.length, "Kontrollerar antal logghändelser");
    for (var j = 0; j < förväntadeHändelser.length; j++) {

        // Speciallösning främst för problemet att en förnyelse av intyg genererar en "Läsa"
        // och en "Skriva" som ofta har samma tidstämpel. Då vet vi inte om det är nuvarande
        // eller nästa händelse i arrayen från mocken som vi ska jämföra mot. Lösning:
        // Peeka på nästa event i händelser för mocken (om vi inte har nått till slutet).
        // Ifall den har samma tidstämpel och den matchar vår förväntade så flyttar vi
        // runt i mockens array för att matcha ordningen
        if (arr[j + 1]) {
            if (arr[j].getElementsByTagName("startdate")[0].innerText === arr[j + 1].getElementsByTagName("startdate")[0].innerText) {
                // Nuvarande och nästa händelse i arrayen från mocken har samma tidstämpel.
                // Kontrollera om nästa händelserna matchar det förväntade eventet. Vi tittar
                // bara på activityType och activityArgs
                if(förväntadeHändelser[j].activity.activityType === arr[j + 1].getElementsByTagName("activitytype")[0].innerText) {
                    var activityArgsNext;
                    if  (arr[j + 1].getElementsByTagName("activityargs")[0]) {
                        activityArgsNext = arr[j + 1].getElementsByTagName("activityargs")[0].innerText;
                    }
                    if (förväntadeHändelser[j].activity.activityArgs === activityArgsNext) {
                        // Både activityArgs och activityType för förväntad händelse stämmer med
                        // händelsen från mocken som kommer i nästa iteration. Byt plats på dem.
                        assert.isTrue(true, "Två händelser har samma tidstämpel och kriterierna för att byta plats är uppfyllda för händelserna på index " + j + " och " + j + 1);
                        [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];
                    }
                }
            }
        }

        // Verifiera att antalet children är rätt, d.v.s att antalet element på högsta nivån stämmer
        assert.equal(arr[j].children.length, 5)

        //---- Element 'Logid' ----//
        var logIdElems = arr[j].getElementsByTagName("logid")
        assert.equal(logIdElems.length, 1, "Kontrollerar antal underelement till 'logid', index " + j);
        assert.isTrue(logIdElems[0].innerText.length > 0, "Kontrollerar att logid existerar, index " + j)

        //---- Element 'System' ----//
        var system = arr[j].getElementsByTagName("system")[0]
        assert.equal(system.children.length, 2, "Kontrollerar antal underelement till 'system', index " + j); // systemid, systemname
        assert.equal(system.getElementsByTagName("systemid")[0].innerText, "SE5565594230-B8N", "Kontrollerar att hårdkodat systemid är satt, index " + j);
        assert.equal(system.getElementsByTagName("systemname")[0].innerText, "Webcert", "Kontrollerar att hårdkodat systemnamn är satt, index " + j);

        //---- Element 'Activity' ----//
        var activity = arr[j].getElementsByTagName("activity")[0];
        var antalFaktiskaElementActivity = activity.children.length;
        var antalFörväntadeElementActivity = 4;
        // ActivityArgs är optional
        if (förväntadeHändelser[j].activity.activityArgs) {
            antalFörväntadeElementActivity = 5;
        }

        // activitytype, activitylevel, activityargs
        assert.equal(antalFaktiskaElementActivity, antalFörväntadeElementActivity, "Kontrollerar antal underelement till 'activity', index " + j);
        assert.equal(activity.getElementsByTagName("activitytype")[0].innerText, förväntadeHändelser[j].activity.activityType, "Kontrollerar aktivitetstyp, index " + j);
        assert.equal(activity.getElementsByTagName("activitylevel")[0].innerText, förväntadeHändelser[j].activity.activityLevel, "Kontrollerar intygsid, index " + j);
        if (förväntadeHändelser[j].activity.activityArgs) {
            assert.equal(activity.getElementsByTagName("activityargs")[0].innerText, förväntadeHändelser[j].activity.activityArgs, "Kontrollerar extra aktivitetsargument, index " + j);
        }

        // Startdate. Verifiera inte exakt tidstämpel, endast att den är i ett giltigt format
        const startDate = activity.getElementsByTagName("startdate")[0].innerText
        assert.notEqual(new Date(startDate), "Invalid Date", "Kontrollerar datumstämpel, index " + j);

        // Purpose
        assert.equal(activity.getElementsByTagName("purpose")[0].innerText, "Vård och behandling", "Kontrollerar hårdkodat 'purpose', index " + j);

        //---- Element 'User' ----//
        var user = arr[j].getElementsByTagName("user")[0];
        // userid, name, assignment, title, careprovider, careunit

        //Kommenterat bort kod som har med elementet name som nu är borttaget ur logsender. Samt ändrad children.length from 6 till 5

        assert.equal(user.children.length, 5, "Kontrollerar antal underelement till 'user', index " + j);
        assert.equal(user.getElementsByTagName("userid")[0].innerText, förväntadeHändelser[j].user.userId, "Kontrollerar userid, index " + j);
        // assert.equal(user.getElementsByTagName("name")[0].innerText, "", "Kontrollerar att name är tomt, index " + j);
        assert.equal(user.getElementsByTagName("assignment")[0].innerText, förväntadeHändelser[j].user.assignment, "Kontrollerar assignment, index " + j);
        assert.equal(user.getElementsByTagName("title")[0].innerText, förväntadeHändelser[j].user.title, "Kontrollerar title, index " + j);

        // Underelement 'Care provider'
        var careprovider = user.getElementsByTagName('careprovider')[0]
        // careproviderid, careprovidername
        assert.equal(careprovider.children.length, 2, "Kontrollerar antal underelement till 'careprovider', index " + j);
        assert.equal(careprovider.getElementsByTagName('careproviderid')[0].innerText,
                        förväntadeHändelser[j].user.careProvider.careProviderId,
                        "Kontrollerar careproviderid, index " + j);
        assert.equal(careprovider.getElementsByTagName('careprovidername')[0].innerText,
                        förväntadeHändelser[j].user.careProvider.careProviderName,
                        "Kontrollerar careprovidername, index " + j);

        // Underelement 'Care Unit'
        var careunit = user.getElementsByTagName('careunit')[0]
        // careunitid, careunitname
        assert.equal(careunit.children.length, 2, "Kontrollerar antal underelement till 'careunit', index " + j);
        assert.equal(careunit.getElementsByTagName('careunitid')[0].innerText,
                        förväntadeHändelser[j].user.careUnit.careUnitId,
                        "Kontrollerar careunitid, index " + j);
        assert.equal(careunit.getElementsByTagName('careunitname')[0].innerText,
                        förväntadeHändelser[j].user.careUnit.careUnitName,
                        "Kontrollerar careunitname, index " + j);

        //---- Element 'Resources' ----//
        // Verifiera att 'resources' endast har ett underelement, 'resource'
        var resources = arr[j].getElementsByTagName("resources")[0];
        assert.equal(resources.children.length, 1, "Kontrollerar antal underelement till 'resources', index " + j);
        var resource = resources.getElementsByTagName('resource')[0];
        // Resource har underelement resourcetype, patient, careprovider, careunit
        assert.equal(resource.children.length, 4, "Kontrollerar antal underelement till 'resource', index " + j);
        assert.equal(resource.getElementsByTagName('resourcetype')[0].innerText, "Intyg", "Kontrollerar hårdkodat 'resourcetype', index " + j);

        // Underelement 'Patient'
        var patient = resource.getElementsByTagName('patient')[0];
        assert.equal(patient.children.length, 1, "Kontrollerar antal underelement till 'patient', index " + j);
        // PatientId har i StoreLog V2 byggts ut till två underelement, "root" och "extension" där den sistnämnda är patientid
        var patientId = patient.getElementsByTagName('patientId')[0];
        assert.equal(patientId.getElementsByTagName('extension')[0].innerText,
                        förväntadeHändelser[j].resources.resource.patient.patientId,
                        "Kontrollerar 'patientid', index " + j);

        // Underelement 'CareProvider'
        var careproviderResources = resource.getElementsByTagName('careprovider')[0];
        assert.equal(careproviderResources.children.length, 2, "Kontrollerar antal underelement till 'careprovider' (under 'resources'), index " + j);
        assert.equal(careproviderResources.getElementsByTagName('careproviderid')[0].innerText,
                        förväntadeHändelser[j].resources.resource.careProvider.careProviderId,
                        "Kontrollerar 'careproviderid' (under 'resources'), index " + j);
        assert.equal(careproviderResources.getElementsByTagName('careprovidername')[0].innerText,
                        förväntadeHändelser[j].resources.resource.careProvider.careProviderName,
                        "Kontrollerar 'careprovidername' (under 'resources'), index " + j);

        // Underelement 'CareUnit'
        var careunitResources = resource.getElementsByTagName('careunit')[0];
        assert.equal(careunitResources.children.length, 2, "Kontrollerar antal underelement till 'careunit' (under 'resources'), index " + j);
        assert.equal(careunitResources.getElementsByTagName('careunitid')[0].innerText,
                        förväntadeHändelser[j].resources.resource.careUnit.careUnitId,
                        "Kontrollerar 'careunitid' (under 'resources'), index " + j);
        assert.equal(careunitResources.getElementsByTagName('careunitname')[0].innerText,
                        förväntadeHändelser[j].resources.resource.careUnit.careUnitName,
                        "Kontrollerar 'careunitname' (under 'resources'), index " + j);
    }
}

// Debug-funktion. Anropa denna för att skriva ut alla aktivitetstyper (e.g. Läsa)
// och argument (om de finns). Använder assert istället för cy.log() för att få ut
// dem i loggen direkt.
function skrivUtHändelser(händelseArray) {
    assert.isTrue(true, "Skriver ut activityType och activityArgs för alla händelser:");
    for (var debugLoop = 0; debugLoop < händelseArray.length; debugLoop++) {
        var debugActivity = händelseArray[debugLoop].getElementsByTagName("activity")[0];

        var debugActivityType = "ActivityType: " + debugActivity.getElementsByTagName("activitytype")[0].innerText;

        var debugActivityArgs = ""
        if (debugActivity.getElementsByTagName("activityargs") && debugActivity.getElementsByTagName("activityargs")[0]) {
            debugActivityArgs = ", activityArgs: " + debugActivity.getElementsByTagName("activityargs")[0].innerText;
        }
        assert.isTrue(true, debugActivityType + debugActivityArgs);
    }
}

/*
Detta command tar in en array med förväntade PDL-loggar. Dessa verifieras mot
riktiga PDL-events som hämtas från loggkälla.
*/
Cypress.Commands.add("verifieraPdlLoggar", pdlLogArray => {
    assert.isTrue(Array.isArray(pdlLogArray));

    // Returnera om det inte finns några element i arrayen att verifiera
    if(!pdlLogArray.length) {
        cy.log("0 element... returnerar direkt.");
        return;
    }

    // Säkerställ att LogSender har skickat alla loggar
    cy.wait(Cypress.env('logSenderTimeout'));

    const mockUrl = Cypress.env('intygMockBaseUrl') + "/validate/cypressAssertPayload/Webcert-pdl-"
    var användarnamn = Cypress.env('MOCK_USERNAME_PASSWORD_USR')
    var lösenord = Cypress.env('MOCK_USERNAME_PASSWORD_PSW')
    // Internt kräver mocken ingen autentisering. Jenkins körs externt så
    // den har användarnamn och lösen satt som miljövariabler
    if (!användarnamn) {
        användarnamn = "";
    }
    if (!lösenord) {
        lösenord = "";
    }

    var förväntadeHändelserPerIntygsid = delaPdlEventsPåIntygsid(pdlLogArray);

    // Speciallösning för att stega i (index) asynkront. Denna måste stegas tillsammans med "vanliga" i
    cy.wrap(0).as('index');

    for (var i = 0; i < förväntadeHändelserPerIntygsid.length; i++) {
        // Hämta alla loggar från mocken
        cy.get('@index').then((ix) => {
            cy.log("URL till mock för att hämta PDL: " + mockUrl + förväntadeHändelserPerIntygsid[ix][0]);
            cy.request({
                method: 'GET',
                url: mockUrl + förväntadeHändelserPerIntygsid[ix][0],
                auth: {
                    username: användarnamn,
                    password: lösenord
                }
            }).then((resp) => {
                expect(resp.status).to.equal(200);
                cy.wrap(resp).its('body').then(function(body) {
                    // Städa bort alla <br>-taggar och alla blanksteg mellan taggar
                    body = body.replace(/<br>/g, "");
                    body = body.replace(/>\s+</g, "><");

                    var bodyDoc = document.createElement("div");
                    bodyDoc.innerHTML = body;

                    var mockHändelser = [].slice.call(bodyDoc.getElementsByTagName("ns2:Log"));
                    sorteraHändelserKronologiskt(mockHändelser);

                    // Debug för att skriva ut alla händelser från mocken i kronologisk ordning
                    // skrivUtHändelser(mockHändelser);

                    // Arrayen med förväntade event innehåller mockens URL på index 0. Skapa ny array från index 1.
                    var förväntadeHändelser = förväntadeHändelserPerIntygsid[ix].slice(1);
                    verifieraHändelserFörIntyg(förväntadeHändelser, mockHändelser);
                    cy.wrap(ix + 1).as('index'); // Del av speciallösningen för att kunna använda index asynkront.
                });
            });
        })
    }
});
