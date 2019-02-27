// For comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands

function loginCaregiver(fx, isDeep) {
	const vårdgivare = fx.vårdgivare;
	const vårdenhet = fx.vårdenhet;
	expect(vårdgivare).to.exist;
	expect(vårdenhet).to.exist;
	assert.isBoolean(isDeep);

	const originString = (isDeep ? "DJUPINTEGRATION" : "NORMAL");

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

// Skapa ett LISJP-utkast via createdraft-anrop och returnera id:t
Cypress.Commands.add("createLisjpDraft", fx => {
	const vårdgivare = fx.vårdgivare;
	const vårdtagare = fx.vårdtagare;
	const vårdenhet = fx.vårdenhet;
	expect(vårdgivare).to.exist;
	expect(vårdtagare).to.exist;
	expect(vårdenhet).to.exist;

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
								<urn2:code>LISJP</urn2:code>\
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
							</urn1:patient>\
							<urn1:skapadAv>\
								<urn1:personal-id>\
									<urn2:root>1.2.752.129.2.1.4.1</urn2:root>\
									<urn2:extension>' + vårdgivare.hsaId + '</urn2:extension>\
								</urn1:personal-id>\
								<urn1:fullstandigtNamn>' + vårdgivare.förnamn + ' ' + vårdgivare.efternamn + '</urn1:fullstandigtNamn>\
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
