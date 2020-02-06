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
    DB: "DB",
    DOI: "DOI"
}
function generateQuickGuid() {
    return Math.random().toString(36).substring(2, 15) +
        Math.random().toString(36).substring(2, 15);
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
Cypress.Commands.add("loggaUt",() => {
  //  loggaUt();
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
            });
        });
    });
}
// Skapa ett förifyllt LISJP
function skapaLISJPIfylltUtkast(fx, intygstyp) {
    const vårdpersonal = fx.vårdpersonal;
    const vårdtagare = fx.vårdtagare;
    const vårdenhet = fx.vårdenhet;
    
    expect(vårdpersonal).to.exist;
    expect(vårdtagare).to.exist;
    expect(vårdenhet).to.exist;

    expect(Object.values(implementeradeIntyg)).to.include.members([intygstyp]);

    const theRequest = cy.request({
        method: 'POST',
        url: '/services/create-draft-certificate/v3.0',
        body:
        '<soapenv:Envelope\
        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
        xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:3" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3" xmlns:urn4="urn:riv:clinicalprocess:healthcond:certificate:3.2" xmlns:urn5="urn:riv:clinicalprocess:healthcond:certificate:3.3">\
        <soapenv:Header>\
            <urn:LogicalAddress>?</urn:LogicalAddress>\
        </soapenv:Header>\
        <soapenv:Body>\
        <urn1:CreateDraftCertificate xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:3" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3" xmlns:urn5="urn:riv:clinicalprocess:healthcond:certificate:3.3">\
        <urn1:intyg>\
            <urn1:typAvIntyg>\
            <urn2:code>'+ intygstyp +'</urn2:code>\
            <urn2:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn2:codeSystem>\
            </urn1:typAvIntyg>\
            <urn1:patient>\
            <urn3:person-id>\
                <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\
                <urn2:extension>'+ vårdtagare.personnummerKompakt +'</urn2:extension>\
            </urn3:person-id>\
            <urn3:fornamn>'+ vårdtagare.förnamn +'</urn3:fornamn>\
            <urn3:efternamn>'+ vårdtagare.efternamn +'</urn3:efternamn>\
            <urn3:postadress>'+ vårdtagare.postadress +'</urn3:postadress>\
            <urn3:postnummer>'+ vårdtagare.postnummer +'</urn3:postnummer>\
            <urn3:postort>'+ vårdtagare.postort +'</urn3:postort>\
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
            <urn1:ref>journal-system-referens</urn1:ref>\
            <urn5:forifyllnad>\
            <urn5:svar id="27">\
                <urn3:delsvar id="27.1">false</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="1">\
                <urn3:instans>1</urn3:instans>\
                <urn3:delsvar id="1.1">\
                <urn2:cv>\
                    <urn2:code>UNDERSOKNING</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0001</urn2:codeSystem>\
                    <urn2:displayName>Min undersökning av patienten</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="1.2">ADADADADADADAD</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="1">\
                <urn3:delsvar id="1.1">\
                <urn2:cv>\
                    <urn2:code>TELEFONKONTAKT</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0001</urn2:codeSystem>\
                    <urn2:displayName>Min telefonkontakt med patienten</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="1.2">2017-05-26</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="1">\
                <urn3:instans>3</urn3:instans>\
                <urn3:delsvar id="1.1">\
                <urn2:cv>\
                    <urn2:code>JOURNALUPPGIFTER</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0001</urn2:codeSystem>\
                    <urn2:displayName>Journaluppgifter från den</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="1">\
                <urn3:instans>4</urn3:instans>\
                <urn3:delsvar id="1.1">\
                <urn2:cv>\
                    <urn2:code>ANNAT</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0001</urn2:codeSystem>\
                    <urn2:displayName>Annat</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="1.2">2017-05-26</urn3:delsvar>\
                <urn3:delsvar id="1.3">baserat på annat</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="28">\
                <urn3:instans>1</urn3:instans>\
                <urn3:delsvar id="28.1">\
                <urn2:cv>\
                    <urn2:code>NUVARANDE_ARBETE</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0002</urn2:codeSystem>\
                    <urn2:displayName>Nuvarande arbete</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="28">\
                <urn3:instans>2</urn3:instans>\
                <urn3:delsvar id="28.1">\
                <urn2:cv>\
                    <urn2:code>ARBETSSOKANDE</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0002</urn2:codeSystem>\
                    <urn2:displayName>Arbetssökande</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="28">\
                <urn3:instans>3</urn3:instans>\
                <urn3:delsvar id="28.1">\
                <urn2:cv>\
                    <urn2:code>FORALDRALEDIG</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0002</urn2:codeSystem>\
                    <urn2:displayName>Föräldraledighet för vård av barn</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="28">\
                <urn3:instans>4</urn3:instans>\
                <urn3:delsvar id="28.1">\
                <urn2:cv>\
                    <urn2:code>STUDIER</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0002</urn2:codeSystem>\
                    <urn2:displayName>Studier</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="29">\
                <urn3:delsvar id="29.1">Ett yrke med arbetsuppgifter</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="6">\
                <urn3:delsvar id="6.2">\
                <urn2:cv>\
                    <urn2:code>M79</urn2:code>\
                    <urn2:codeSystem>1.2.752.116.1.1.1.1.3</urn2:codeSystem>\
                    <urn2:displayName>Icke specificerad akut infektion i nedre luftvägarna</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="6.1">Icke specificerad akut infektion i nedre luftvägarna</urn3:delsvar>\
                <urn3:delsvar id="6.4">\
                <urn2:cv>\
                    <urn2:code>M46</urn2:code>\
                    <urn2:codeSystem>1.2.752.116.1.1.1.1.3</urn2:codeSystem>\
                    <urn2:displayName>Andra inflammatoriska sjukdomar i ryggraden</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="6.3">Andra inflammatoriska sjukdomar i ryggraden</urn3:delsvar>\
                <urn3:delsvar id="6.6">\
                <urn2:cv>\
                    <urn2:code>S22</urn2:code>\
                    <urn2:codeSystem>1.2.752.116.1.1.1.1.3</urn2:codeSystem>\
                    <urn2:displayName>234Fraktur på revben, bröstbenet och bröstkotpelaren</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="6.5"></urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="35">\
                <urn3:delsvar id="35.1">Funktionell nedsättning</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="17">\
                <urn3:delsvar id="17.1">Begränsning i aktivitet</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="19">\
                <urn3:delsvar id="19.1">En pågående behandling</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="20">\
                <urn3:delsvar id="20.1">En planerad behandling</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="32">\
                <urn3:instans>1</urn3:instans>\
                <urn3:delsvar id="32.1">\
                <urn2:cv>\
                    <urn2:code>HELT_NEDSATT</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0003</urn2:codeSystem>\
                    <urn2:displayName>100%</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="32.2">\
                <urn2:datePeriod>\
                    <urn2:start>2017-06-19</urn2:start>\
                    <urn2:end>2017-07-10</urn2:end>\
                </urn2:datePeriod>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="32">\
                <urn3:instans>2</urn3:instans>\
                <urn3:delsvar id="32.1">\
                <urn2:cv>\
                    <urn2:code>TRE_FJARDEDEL</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0003</urn2:codeSystem>\
                    <urn2:displayName>75%</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="32.2">\
                <urn2:datePeriod>\
                    <urn2:start>2017-05-28</urn2:start>\
                    <urn2:end>2017-06-18</urn2:end>\
                </urn2:datePeriod>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="32">\
                <urn3:instans>3</urn3:instans>\
                <urn3:delsvar id="32.1">\
                <urn2:cv>\
                    <urn2:code>HALFTEN</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0003</urn2:codeSystem>\
                    <urn2:displayName>50%</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="32.2">\
                <urn2:datePeriod>\
                    <urn2:start>2017-05-27</urn2:start>\
                    <urn2:end>2017-05-27</urn2:end>\
                </urn2:datePeriod>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="32">\
                <urn3:instans>4</urn3:instans>\
                <urn3:delsvar id="32.1">\
                <urn2:cv>\
                    <urn2:code>EN_FJARDEDEL</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0003</urn2:codeSystem>\
                    <urn2:displayName>25%</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="32.2">\
                <urn2:datePeriod>\
                    <urn2:start>2017-05-26</urn2:start>\
                    <urn2:end>2017-05-26</urn2:end>\
                </urn2:datePeriod>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="37">\
                <urn3:delsvar id="37.1">Arbetsförmågan bedöms nedsatt en längre tid.</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="33">\
                <urn3:delsvar id="33.1">true</urn3:delsvar>\
                <urn3:delsvar id="33.2">Ett medicinskt skäl till annan förläggning</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="3999">\
                <urn3:delsvar id="34.1">true</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="39">\
                <urn3:delsvar id="39.1">\
                <urn2:cv>\
                    <urn2:code>ATER_X_ANTAL_DGR</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0006</urn2:codeSystem>\
                    <urn2:displayName>Patienten kommer med stor sannolikhet att återgå helt i nuvarande sysselsättning efter x antal dagar</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
                <urn3:delsvar id="39.3">\
                <urn2:cv>\
                    <urn2:code>NITTIO_DGR</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0007</urn2:codeSystem>\
                    <urn2:displayName>90 dagar</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>1</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>ARBETSTRANING</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Arbetsträning</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>2</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>ARBETSANPASSNING</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Arbetsanpassning</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>3</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>SOKA_NYTT_ARBETE</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Söka nytt arbete</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>4</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>BESOK_ARBETSPLATS</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Besök på arbetsplatsen</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>5</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>ERGONOMISK</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Ergonomisk bedömning</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>6</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>HJALPMEDEL</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Hjälpmedel</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>7</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>KONFLIKTHANTERING</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Konflikthantering</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>8</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>KONTAKT_FHV</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Kontakt med företagshälsovård</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>9</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>OMFORDELNING</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Omfördelning av arbetsuppgifter</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="40">\
                <urn3:instans>10</urn3:instans>\
                <urn3:delsvar id="40.1">\
                <urn2:cv>\
                    <urn2:code>OVRIGA_ATGARDER</urn2:code>\
                    <urn2:codeSystem>KV_FKMU_0004</urn2:codeSystem>\
                    <urn2:displayName>Övrigt</urn2:displayName>\
                </urn2:cv>\
                </urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="44">\
                <urn3:delsvar id="44.1">Övriga åtgärder finns</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="25">\
                <urn3:delsvar id="25.1">En del övrigt om patienten</urn3:delsvar>\
            </urn5:svar>\
            <urn5:svar id="26">\
                <urn3:delsvar id="26.1">true</urn3:delsvar>\
                <urn3:delsvar id="26.2">Gillar att prata i telefon</urn3:delsvar>\
            </urn5:svar>\
            </urn5:forifyllnad>\
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
function skapaKomplettering(fx) {
    const vårdpersonal = fx.vårdpersonal;
    const vårdtagare = fx.vårdtagare;
    const vårdenhet = fx.vårdenhet;
    const intygsID = fx.utkastId
    const meddelandeId = generateQuickGuid();
    expect(intygsID).to.exist;
    expect(vårdpersonal).to.exist;
    expect(vårdtagare).to.exist;
    expect(vårdenhet).to.exist;
    //expect(Object.values(implementeradeIntyg)).to.include.members([intygstyp]);
    cy.log(vårdtagare.personnummerKompakt + vårdtagare.förnamn +vårdtagare.efternamn + vårdtagare.postadress + vårdtagare.postnummer + vårdtagare.postort);
    cy.request({
        method: 'POST',
        url: '/services/send-message-to-care/v2.0',
        body:'<soapenv:Envelope\
        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
        xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3">\
        <soapenv:Header>\
                <urn:LogicalAddress>?</urn:LogicalAddress>\
            </soapenv:Header>\
            <soapenv:Body>\
                <urn1:SendMessageToCare>\
                    <urn1:meddelande-id>'+ meddelandeId +'</urn1:meddelande-id>\
                    <urn1:skickatTidpunkt>2016-07-13T17:23:00</urn1:skickatTidpunkt>\
                    <urn1:intygs-id>\
                        <urn2:root />\
                        <urn2:extension>'+ intygsID +'</urn2:extension>\
                    </urn1:intygs-id>\
                    <urn1:patientPerson-id>\
                        <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\
                        <urn2:extension>'+ vårdtagare.personnummerKompakt +'</urn2:extension>\
                    </urn1:patientPerson-id>\
                    <urn1:logiskAdressMottagare>'+ vårdenhet +'</urn1:logiskAdressMottagare>\
                    <urn1:amne>\
                        <urn2:code>KOMPLT</urn2:code>\
                        <urn2:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</urn2:codeSystem>\
                    </urn1:amne>\
                    <urn1:rubrik>Komplettering</urn1:rubrik>\
                    <urn1:meddelande>Komplettering</urn1:meddelande>\
                    <urn1:skickatAv>\
                        <urn1:part>\
                        <urn2:code>FKASSA</urn2:code>\
                        <urn2:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</urn2:codeSystem>\
                        </urn1:part>\
                    </urn1:skickatAv>\
                    <urn1:komplettering>\
                        <urn1:frage-id>1</urn1:frage-id>\
                        <urn1:instans>1</urn1:instans>\
                        <urn1:text>Detta är kompletteringstexten...</urn1:text>\
                    </urn1:komplettering>\
                </urn1:SendMessageToCare>\
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
                
                // Utan detta klagar Cypress på att man blandar synkron och asynkron kod
                cy.wrap(resultCode).then((resCode) => {
                    return resCode;
                })
                
            });
        });
}
function skickaRegisterLisjp(fx) {

    const vårdpersonal = fx.vårdpersonal;
    const vårdtagare = fx.vårdtagare;
    const vårdenhet = fx.vårdenhet;
    const intygsID = "LISJP" +generateQuickGuid();
    const intygsUrl = Cypress.env('intygTjanstUrl') + "/inera-certificate/register-certificate-se/v3.0"
    cy.log(intygsID);
    expect(vårdpersonal).to.exist;
    expect(vårdtagare).to.exist;
    expect(vårdenhet).to.exist;
    //expect(Object.values(implementeradeIntyg)).to.include.members([intygstyp]);
    //cy.log(vårdtagare.personnummerKompakt + vårdtagare.förnamn +vårdtagare.efternamn + vårdtagare.postadress + vårdtagare.postnummer + vårdtagare.postort);
    cy.request({
        method: 'POST',
        url: intygsUrl,
        body:'<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">\
        <soap:Header>\
        <ns7:LogicalAddress  xmlns:ns7="urn:riv:itintegration:registry:1" xmlns:ns6="urn:riv:clinicalprocess:healthcond:certificate:3.2" xmlns:ns5="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:ns4="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3" xmlns:ns3="urn:riv:clinicalprocess:healthcond:certificate:3" xmlns:dsf="http://www.w3.org/2002/06/xmldsig-filter2 " xmlns:ds="http://www.w3.org/2000/09/xmldsig#"></ns7:LogicalAddress>\
        </soap:Header>\
        <soap:Body>\
            <ns4:RegisterCertificate  xmlns:ds="http://www.w3.org/2000/09/xmldsig# " xmlns:dsf="http://www.w3.org/2002/06/xmldsig-filter2 " xmlns:ns3="urn:riv:clinicalprocess:healthcond:certificate:3" xmlns:ns4="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3" xmlns:ns5="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:ns6="urn:riv:clinicalprocess:healthcond:certificate:3.2" xmlns:ns7="urn:riv:itintegration:registry:1">\
                <ns4:intyg>\
                    <ns3:intygs-id>\
                        <ns5:root>'+ vårdenhet.id +'</ns5:root>\
                        <ns5:extension>'+ intygsID +'</ns5:extension>\
                    </ns3:intygs-id>\
                    <ns3:typ>\
                        <ns5:code>LISJP</ns5:code>\
                        <ns5:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</ns5:codeSystem>\
                        <ns5:displayName>Läkarintyg för sjukpenning</ns5:displayName>\
                    </ns3:typ>\
                    <ns3:version>1.1</ns3:version>\
                    <ns3:signeringstidpunkt>2020-02-02T08:39:40</ns3:signeringstidpunkt>\
                    <ns3:skickatTidpunkt>2020-02-02T08:39:40</ns3:skickatTidpunkt>\
                    <ns3:patient>\
                        <ns3:person-id>\
                            <ns5:root>1.2.752.129.2.1.3.1</ns5:root>\
                            <ns5:extension>'+ vårdtagare.personnummerKompakt +'</ns5:extension>\
                        </ns3:person-id>\
                        <ns3:fornamn/>\
                        <ns3:efternamn/>\
                        <ns3:postadress/>\
                        <ns3:postnummer/>\
                        <ns3:postort/>\
                    </ns3:patient>\
                    <ns3:skapadAv>\
                        <ns3:personal-id>\
                            <ns5:root>1.2.752.129.2.1.4.1</ns5:root>\
                            <ns5:extension>'+  vårdpersonal.id +'</ns5:extension>\
                        </ns3:personal-id>\
                        <ns3:fullstandigtNamn>'+ vårdpersonal.förnamn + " " + vårdpersonal.efternamn + intygsID +'</ns3:fullstandigtNamn>\
                        <ns3:forskrivarkod>0000000</ns3:forskrivarkod>\
                        <ns3:befattning>\
                            <ns5:code>203090</ns5:code>\
                            <ns5:codeSystem>1.2.752.129.2.2.1.4</ns5:codeSystem>\
                            <ns5:displayName>Läkare legitimerad, annan</ns5:displayName>\
                        </ns3:befattning>\
                        <ns3:enhet>\
                            <ns3:enhets-id>\
                                <ns5:root>1.2.752.129.2.1.4.1</ns5:root>\
                                <ns5:extension>' + vårdenhet.id +'</ns5:extension>\
                            </ns3:enhets-id>\
                            <ns3:arbetsplatskod>\
                                <ns5:root>1.2.752.29.4.71</ns5:root>\
                                <ns5:extension>0000000</ns5:extension>\
                            </ns3:arbetsplatskod>\
                            <ns3:enhetsnamn>Ivars integrationsenhet 2</ns3:enhetsnamn>\
                            <ns3:postadress>Bryggaregatan 11</ns3:postadress>\
                            <ns3:postnummer>65340</ns3:postnummer>\
                            <ns3:postort>Karlstad</ns3:postort>\
                            <ns3:telefonnummer>054100000</ns3:telefonnummer>\
                            <ns3:epost>intygnmt@nordicmedtest.se</ns3:epost>\
                            <ns3:vardgivare>\
                                <ns3:vardgivare-id>\
                                    <ns5:root>1.2.752.129.2.1.4.1</ns5:root>\
                                    <ns5:extension>' +  vårdenhet.vårdgivareId +'</ns5:extension>\
                                </ns3:vardgivare-id>\
                                <ns3:vardgivarnamn>VG_TestAutomation</ns3:vardgivarnamn>\
                            </ns3:vardgivare>\
                        </ns3:enhet>\
                    </ns3:skapadAv>\
                    <ns3:svar id="27">\
                        <ns3:delsvar id="27.1">false</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="1">\
                        <ns3:instans>1</ns3:instans>\
                        <ns3:delsvar id="1.1">\
                            <ns5:cv>\
                                <ns5:code>UNDERSOKNING</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0001</ns5:codeSystem>\
                                <ns5:displayName>Min undersökning av patienten</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="1.2">2020-02-03</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="1">\
                        <ns3:instans>2</ns3:instans>\
                        <ns3:delsvar id="1.1">\
                            <ns5:cv>\
                                <ns5:code>TELEFONKONTAKT</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0001</ns5:codeSystem>\
                                <ns5:displayName>Min telefonkontakt med patienten</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="1.2">2018-12-03</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="1">\
                        <ns3:instans>3</ns3:instans>\
                        <ns3:delsvar id="1.1">\
                            <ns5:cv>\
                                <ns5:code>JOURNALUPPGIFTER</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0001</ns5:codeSystem>\
                                <ns5:displayName>Journaluppgifter från den</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="1.2">2018-12-03</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="1">\
                        <ns3:instans>4</ns3:instans>\
                        <ns3:delsvar id="1.1">\
                            <ns5:cv>\
                                <ns5:code>ANNAT</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0001</ns5:codeSystem>\
                                <ns5:displayName>Annat</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="1.2">2018-12-03</ns3:delsvar>\
                        <ns3:delsvar id="1.3">Journal from down below</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="28">\
                        <ns3:instans>1</ns3:instans>\
                        <ns3:delsvar id="28.1">\
                            <ns5:cv>\
                                <ns5:code>NUVARANDE_ARBETE</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0002</ns5:codeSystem>\
                                <ns5:displayName>Nuvarande arbete</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="28">\
                        <ns3:instans>2</ns3:instans>\
                        <ns3:delsvar id="28.1">\
                            <ns5:cv>\
                                <ns5:code>ARBETSSOKANDE</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0002</ns5:codeSystem>\
                                <ns5:displayName>Arbetssökande</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="28">\
                        <ns3:instans>3</ns3:instans>\
                        <ns3:delsvar id="28.1">\
                            <ns5:cv>\
                                <ns5:code>FORALDRALEDIG</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0002</ns5:codeSystem>\
                                <ns5:displayName>Föräldraledighet för vård av barn</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="28">\
                        <ns3:instans>4</ns3:instans>\
                        <ns3:delsvar id="28.1">\
                            <ns5:cv>\
                                <ns5:code>STUDIER</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0002</ns5:codeSystem>\
                                <ns5:displayName>Studier</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="29">\
                        <ns3:delsvar id="29.1">Blomplockare på fritiden endast</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="6">\
                        <ns3:delsvar id="6.2">\
                            <ns5:cv>\
                                <ns5:code>S47</ns5:code>\
                                <ns5:codeSystem>1.2.752.116.1.1.1.1.3</ns5:codeSystem>\
                                <ns5:displayName>Klämskada på skuldra och överarm</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="6.1">Klämskada på skuldra och överarm</ns3:delsvar>\
                        <ns3:delsvar id="6.4">\
                            <ns5:cv>\
                                <ns5:code>Y1113</ns5:code>\
                                <ns5:codeSystem>1.2.752.116.1.1.1.1.3</ns5:codeSystem>\
                                <ns5:displayName>Förgiftning med och exponering för antiepileptika, lugnande läkemedel och sömnmedel, medel mot parkinsonism samt psykotropa läkemedel som ej klassificeras annorstädes, med oklar avsikt-institutionellt boende-annan sysselsättning</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="6.3">Förgiftning med och exponering för antiepileptika, lugnande läkemedel och sömnmedel, medel mot parkinsonism samt psykotropa läkemedel som ej klassificeras annorstädes, med oklar avsikt-institutionellt boende-annan sysselsättning</ns3:delsvar>\
                        <ns3:delsvar id="6.6">\
                            <ns5:cv>\
                                <ns5:code>M659B</ns5:code>\
                                <ns5:codeSystem>1.2.752.116.1.1.1.1.3</ns5:codeSystem>\
                                <ns5:displayName>Ospecifik synovit/tenosynovit i axelled/överarm</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="6.5">Ospecifik synovit/tenosynovit i axelled/överarm</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="35">\
                        <ns3:delsvar id="35.1">Problem som påverkar patientens möjlighet att utföra sin sysselsättning:Energinivå, motivation, aptit, begär, impulskontroll - Sömn - Uppmärksamhet - Smärta - Ledrörlighet - Muskelkraft - MuskeluthållighetKan inte plocka blommor.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="17">\
                        <ns3:delsvar id="17.1">Svårigheter som påverkar patientens sysselsättning:Lyfta och bära föremål - Användning av hand och arm - Hantera stress och andra psykologiska krav - Använda handens finmotorik - Tvätta sig - Sköta toalettbehov - Klä sig - Sköta sin hälsaSe föregående.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="19">\
                        <ns3:delsvar id="19.1">Smörjer med diverse krämer.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="20">\
                        <ns3:delsvar id="20.1">Mer krämer.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="32">\
                        <ns3:instans>1</ns3:instans>\
                        <ns3:delsvar id="32.1">\
                            <ns5:cv>\
                                <ns5:code>HELT_NEDSATT</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0003</ns5:codeSystem>\
                                <ns5:displayName>100%</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="32.2">\
                            <ns5:datePeriod>\
                                <ns5:start>2018-07-01</ns5:start>\
                                <ns5:end>2020-01-01</ns5:end>\
                            </ns5:datePeriod>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                            <ns3:svar id="37">\
                        <ns3:delsvar id="37.1">Det krävdes mer kräm.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="34">\
                        <ns3:delsvar id="34.1">true</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="39">\
                        <ns3:delsvar id="39.1">\
                            <ns5:cv>\
                                <ns5:code>ATER_X_ANTAL_DGR</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0006</ns5:codeSystem>\
                                <ns5:displayName>Patienten kommer med stor sannolikhet att återgå helt i nuvarande sysselsättning efter x antal dagar</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                        <ns3:delsvar id="39.3">\
                            <ns5:cv>\
                                <ns5:code>NITTIO_DGR</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0007</ns5:codeSystem>\
                                <ns5:displayName>90 dagar</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>1</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>ARBETSTRANING</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Arbetsträning</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>2</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>ARBETSANPASSNING</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Arbetsanpassning</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>3</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>SOKA_NYTT_ARBETE</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Söka nytt arbete</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>4</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>BESOK_ARBETSPLATS</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Besök på arbetsplatsen</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>5</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>ERGONOMISK</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Ergonomisk bedömning</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>6</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>HJALPMEDEL</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Hjälpmedel</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>7</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>KONFLIKTHANTERING</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Konflikthantering</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>8</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>KONTAKT_FHV</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Kontakt med företagshälsovård</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>9</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>OMFORDELNING</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Omfördelning av arbetsuppgifter</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="40">\
                        <ns3:instans>10</ns3:instans>\
                        <ns3:delsvar id="40.1">\
                            <ns5:cv>\
                                <ns5:code>OVRIGA_ATGARDER</ns5:code>\
                                <ns5:codeSystem>KV_FKMU_0004</ns5:codeSystem>\
                                <ns5:displayName>Övrigt</ns5:displayName>\
                            </ns5:cv>\
                        </ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="44">\
                        <ns3:delsvar id="44.1">Träna på att arbeta.</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="25">\
                        <ns3:delsvar id="25.1">!#$%&amp;*+,-./0123456789:&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^)_`abcdefghijklmnopqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ)</ns3:delsvar>\
                    </ns3:svar>\
                    <ns3:svar id="26">\
                        <ns3:delsvar id="26.1">true</ns3:delsvar>\
                        <ns3:delsvar id="26.2">Har väntat 12 år på att få öppna min löfbergs lila</ns3:delsvar>\
                    </ns3:svar>\
                </ns4:intyg>\
            </ns4:RegisterCertificate>\
        </soap:Body>\
    </soap:Envelope>'
    }).then((resp) => {
        expect(resp.status).to.equal(200);

        cy.wrap(resp).its('body').then((body) => {

            // Check för att konstatera att responsen på tjänsteanropet innehåller resultCode och att värdet är OK
            expect(body).to.contain("resultCode");
            
            var resultCodeStart = "<ns3:resultCode>"
            var resultCodeEnd = "</ns3:resultCode>"
            var resultCodeStartIdx = body.indexOf(resultCodeStart);
            var resultCodeEndIdx = body.indexOf(resultCodeEnd);
            var resultCode = body.substring(resultCodeStartIdx + resultCodeStart.length, resultCodeEndIdx);
            expect(resultCode).to.equal("OK");
            cy.log(resultCode);
            
            // Utan detta klagar Cypress på att man blandar synkron och asynkron kod
            cy.wrap(resultCode).then((result) => {
                if(result == 'OK'){
                    return intygsID;
                }
            });
            
        });
    });
}
function taBortIntyg(fx) {
   
    const intygsID = fx.intygsID
    const intygsUrl = Cypress.env('intygTjanstUrl') + "/inera-certificate/resources/certificate/" + intygsID;
 
     cy.log(intygsID);
    cy.request({
        method: 'DELETE',
        url: intygsUrl,
        }).then((resp) =>{
            expect(resp.status).to.equal(200);
    });        
            
}
Cypress.Commands.add("taBortIntyg", fx => {
    taBortIntyg(fx);   

});
Cypress.Commands.add("skickaRegisterLisjp", fx => {
    skickaRegisterLisjp(fx);   

});

//Cypress.Commands.add("generateQuickGuid",() =>{
  //  return generateQuickGuid();
//});
Cypress.Commands.add("skapaKomplettering", fx => {
    return skapaKomplettering(fx);
});     
// Skapa ett förifyllt LISJP-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("skapaLISJPIfylltUtkast", fx => {
    return skapaLISJPIfylltUtkast(fx, implementeradeIntyg.LISJP);
});
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

Cypress.Commands.add("skapaDOIUtkast", fx => {
    return skapaUtkast(fx, implementeradeIntyg.DOI);
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
        // activitytype, activitylevel, activityargs
        var activity = arr[j].getElementsByTagName("activity")[0];
        assert.equal(activity.getElementsByTagName("activitytype")[0].innerText, förväntadeHändelser[j].activity.activityType, "Kontrollerar aktivitetstyp, index " + j);
        assert.equal(activity.getElementsByTagName("activitylevel")[0].innerText, förväntadeHändelser[j].activity.activityLevel, "Kontrollerar intygsid, index " + j);

        var antalFaktiskaElementActivity = activity.children.length;
        var antalFörväntadeElementActivity = 4;
        // ActivityArgs är optional
        if (förväntadeHändelser[j].activity.activityArgs) {
            antalFörväntadeElementActivity = 5;
        }

        assert.equal(antalFaktiskaElementActivity, antalFörväntadeElementActivity, "Kontrollerar antal underelement till 'activity', index " + j);
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
