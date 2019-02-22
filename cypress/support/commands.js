// For comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands

Cypress.Commands.add("loginArnoldNormal", () => {
	cy.request({
		method: 'POST',
		url: '/fake',
		form: true,
		body: {
			"all": "all",
			"userJsonDisplay": '{"hsaId":"TSTNMT2321000156-1079","forNamn": "Arnold","efterNamn": "Johansson","enhetId": "TSTNMT2321000156-1077","legitimeradeYrkesgrupper":["Läkare"],"origin": "NORMAL","authenticationMethod": "FAKE"}',
			"origin": "NORMAL"
		}
	}).then((resp) => {
		expect(resp.status).to.equal(200);
	});
});

Cypress.Commands.add("loginArnoldDeep", () => {
	cy.request({
		method: 'POST',
		url: '/fake',
		form: true,
		body: {
			"userJsonDisplay": '{"hsaId":"TSTNMT2321000156-1079","forNamn": "Arnold","efterNamn": "Johansson","enhetId": "TSTNMT2321000156-1077","legitimeradeYrkesgrupper":["Läkare"],"origin": "DJUPINTEGRATION","authenticationMethod": "FAKE"}'
		}
	}).then((resp) => {
		expect(resp.status).to.equal(200);
	});
});

// Ganska specifik funktion. Överväg att flytta till spec där den används (om ej i flera specs)
Cypress.Commands.add("loginArnoldOchGaTillValjPatient", () => {
	cy.loginArnoldNormal().then(() => {
		cy.visit('/#/create/choose-patient/index');
	})
})

// Ganska specifik funktion. Överväg att flytta till spec där den används (om ej i flera specs)
Cypress.Commands.add("goToCreateCertForTolvanAsArnold", () => {
	cy.loginArnoldNormal().then(() => {
		cy.visit('/#/create/choose-intyg-type/19121212-1212/index');
	});
});

// Denna skapar en LISJP för Tolvan Tolvansson som Arnold på Alfa-enheten.
Cypress.Commands.add("createLisjpDraftNonGeneric", () => {
	cy.request({
		method: 'POST',
		url: '/services/create-draft-certificate/v3.0',
		body: '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:3" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3">\r\n   <soapenv:Header>\r\n      <urn:LogicalAddress><!-- T_SERVICES_SE165565594230-109D -->?</urn:LogicalAddress>\r\n   </soapenv:Header>\r\n   <soapenv:Body>\r\n      <urn1:CreateDraftCertificate>\r\n         <urn1:intyg>\r\n            <urn1:typAvIntyg>\r\n               <urn2:code>LISJP</urn2:code>\r\n               <urn2:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn2:codeSystem>\r\n               <!--Optional:-->\r\n               <urn2:displayName>?</urn2:displayName>\r\n            </urn1:typAvIntyg>\r\n            <urn1:patient>\r\n               <urn3:person-id>\r\n                  <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\r\n                  <urn2:extension>191212121212</urn2:extension>\r\n               </urn3:person-id>\r\n               <urn3:fornamn>Tolvan</urn3:fornamn>\r\n               <urn3:efternamn>Tolvansson</urn3:efternamn>\r\n               <!--Optional:-->\r\n               <!--\r\n               <urn3:mellannamn>NMT</urn3:mellannamn>\r\n               <urn3:postadress>Testadress 1</urn3:postadress>\r\n               <urn3:postnummer>11827</urn3:postnummer>\r\n               <urn3:postort>STOCKHOLM</urn3:postort>\r\n               -->\r\n               <!--You may enter ANY elements at this point-->\r\n            </urn1:patient>\r\n            <urn1:skapadAv>\r\n               <urn1:personal-id>\r\n                  <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\r\n                  <urn2:extension>TSTNMT2321000156-1079</urn2:extension>\r\n               </urn1:personal-id>\r\n               <urn1:fullstandigtNamn>Arnold Johansson</urn1:fullstandigtNamn>\r\n               <urn1:enhet>\r\n                  <urn1:enhets-id>\r\n                     <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\r\n                     <urn2:extension>TSTNMT2321000156-1077</urn2:extension>\r\n                  </urn1:enhets-id>\r\n                  <urn1:enhetsnamn><!-- nmtAT_vg1_ve1-->Alfa-enheten</urn1:enhetsnamn>\r\n                  <!--You may enter ANY elements at this point-->\r\n               </urn1:enhet>\r\n               <!--You may enter ANY elements at this point-->\r\n            </urn1:skapadAv>\r\n            <!--You may enter ANY elements at this point-->\r\n         </urn1:intyg>\r\n         <!--You may enter ANY elements at this point-->\r\n      </urn1:CreateDraftCertificate>\r\n   </soapenv:Body>\r\n</soapenv:Envelope>'
	}).then((resp) => {
		expect(resp.status).to.equal(200);

		cy.wrap(resp).its('body').then((body) => {
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
});
