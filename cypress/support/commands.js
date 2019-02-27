// For comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands

function loginCaregiver(fx, isDeep) {
	var originString = "NORMAL";
	if (isDeep === true) {
		originString = "DJUPINTEGRATION";
	}

	cy.request({
		method: 'POST',
		url: '/fake',
		form: true,
		body: {
			"userJsonDisplay":
				'{"hsaId": "' + fx.vårdgivare.hsaId + '",\
				"forNamn": "' + fx.vårdgivare.förnamn + '",\
				"efterNamn": "' + fx.vårdgivare.efternamn +'",\
				"enhetId": "' + fx.vårdenhet.id + '",\
				"legitimeradeYrkesgrupper": ' + fx.vårdgivare.legitimeradeYrkesgrupper + ',\
				"origin": "' + originString + '",\
				"authenticationMethod": "FAKE"}'
		}
	}).then((resp) => {
		expect(resp.status).to.equal(200);
	});
}

Cypress.Commands.add("loggaInVårdgivareNormal", fx => {
	loginCaregiver(fx, false);
});

Cypress.Commands.add("loggaInVårdgivareIntegrerat", fx => {
	loginCaregiver(fx, true);
});

// Denna skapar en LISJP för Tolvan Tolvansson som Arnold på Alfa-enheten.
Cypress.Commands.add("createLisjpDraftNonGeneric", fx => {
	cy.request({
		method: 'POST',
		url: '/services/create-draft-certificate/v3.0',
		body:
			'<soapenv:Envelope\
			xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
			xmlns:urn="urn:riv:itintegration:registry:1" xmlns:urn1="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:3" xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:types:3" xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:3">\
			    <soapenv:Header>\
					<urn:LogicalAddress><!-- T_SERVICES_SE165565594230-109D -->?</urn:LogicalAddress>\
				</soapenv:Header>\
				<soapenv:Body>\
								 <urn1:CreateDraftCertificate>\
										  <urn1:intyg>\
													  <urn1:typAvIntyg>\
																	 <urn2:code>LISJP</urn2:code>\
																					<urn2:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn2:codeSystem>\
																								   <!--Optional:-->\
																												  <urn2:displayName>?</urn2:displayName>\
																															  </urn1:typAvIntyg>\
																																		  <urn1:patient>\
																																						 <urn3:person-id>\
																																										   <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\
																																															 <urn2:extension>191212121212</urn2:extension>\
																																																			</urn3:person-id>\
																																																						   <urn3:fornamn>Tolvan</urn3:fornamn>\
																																																										  <urn3:efternamn>Tolvansson</urn3:efternamn>\
																																																														 <!--Optional:-->\
																																																																		<!--\
																																																																					   <urn3:mellannamn>NMT</urn3:mellannamn>\
																																																																					                  <urn3:postadress>Testadress 1</urn3:postadress>\
																																																																					                  <urn3:postnummer>11827</urn3:postnummer>\
																																																																					                  <urn3:postort>STOCKHOLM</urn3:postort>\
																																																																					                  -->\
																																																																					                  <!--You may enter ANY elements at this point-->\
																																																																					               </urn1:patient>\
																																																																					               <urn1:skapadAv>\
																																																																					                  <urn1:personal-id>\
																																																																					                     <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\
																																															                   <urn2:extension>TSTNMT2321000156-1079</urn2:extension>\
																																															                </urn1:personal-id>\
																																															                <urn1:fullstandigtNamn>Arnold Johansson</urn1:fullstandigtNamn>\
																																															                <urn1:enhet>\
																																															                   <urn1:enhets-id>\
																																															                      <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\
																																															                      <urn2:extension>TSTNMT2321000156-1077</urn2:extension>\
																																															                   </urn1:enhets-id>\
																																															                   <urn1:enhetsnamn><!-- nmtAT_vg1_ve1-->Alfa-enheten</urn1:enhetsnamn>\
																																															                   <!--You may enter ANY elements at this point-->\
																																															                </urn1:enhet>\
																																															                <!--You may enter ANY elements at this point-->\
																																															             </urn1:skapadAv>\
																																															             <!--You may enter ANY elements at this point-->\
																																															          </urn1:intyg>\
																																															          <!--You may enter ANY elements at this point-->\
																																															       </urn1:CreateDraftCertificate>\
																																															    </soapenv:Body>\
																																															 </soapenv:Envelope>'
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
