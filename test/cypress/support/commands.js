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
}

function loggaInVårdpersonal(fx, ärDjup) {
	const vårdpersonal = fx.vårdpersonal;
	const vårdenhet = fx.vårdenhet;
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
				'{"hsaId": "' + fx.vårdpersonal.hsaId + '",\
				"forNamn": "' + fx.vårdpersonal.förnamn + '",\
				"efterNamn": "' + fx.vårdpersonal.efternamn +'",\
				"enhetId": "' + fx.vårdenhet.id + '",\
				"legitimeradeYrkesgrupper": ' + fx.vårdpersonal.legitimeradeYrkesgrupper + ',\
				"origin": "' + originSträng + '",\
				"authenticationMethod": "FAKE"}'
		}
	}).then((resp) => {
		expect(resp.status).to.equal(200);
	});
}

Cypress.Commands.add("loggaInVårdpersonalNormal", fx => {
	loggaInVårdpersonal(fx, false);
});

Cypress.Commands.add("loggaInVårdpersonalIntegrerat", fx => {
	loggaInVårdpersonal(fx, true);
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


Cypress.Commands.add("verifieraPdlLoggar", pdlLogArray => {

	cy.log("cy.verifieraPdlLoggar - enter. Antal loggar: " + pdlLogArray.length)

	// Returnera om det inte finns några element i arrayen att verifiera
	if(pdlLogArray === undefined || pdlLogArray.length === 0) {
		cy.log("undefined array eller 0 element... returnerar bara.")
		return
	}

    // Konstanterna ska brytas ut till ex.vis cypress.json
    const logSenderTimeout = 15000;
	const mockBaseUrl = "http://mocks.sm.nordicmedtest.se:43000/validate/cypressAssertPayload/Webcert-pdl-"

    // Säkerställ att LogSender har skickat alla loggar
    cy.wait(logSenderTimeout);

	// Loopa igenom arrayen och plocka ut antal unika intygsid:n (detta påverkar vilka URL:er vi ska hämta loggar från)
	// Skapa en tvådimensionell array med intygsid som första element på varje plats, följt av logghändelserna som ska verifieras.
	var idSplitArray = []
	for (var i = 0; i < pdlLogArray.length; i++) {
		var nyttId = true
		for (var j = 0; j < idSplitArray.length; j++) {
			if (idSplitArray[j][0] === pdlLogArray[i].activity.activityLevel) {
				nyttId = false
				idSplitArray[j].push(pdlLogArray[i]);
				break;
			}
		}

		if (nyttId) {
			// Vi hittade inte intygsId:t i idSplitArray vilket betyder att det är ett nytt id som ska läggas till
			var nyttIndex = idSplitArray.length
			idSplitArray[nyttIndex] = []
			idSplitArray[nyttIndex].push(pdlLogArray[i].activity.activityLevel)
			idSplitArray[nyttIndex].push(pdlLogArray[i])
		}
	}

	cy.log("Vi hittade " + idSplitArray.length + " unika intygsid:n i arrayen")

	// Extra sanity check. Denna kan tas bort när det funkar pålitligt.
	for (var i = 0; i < idSplitArray.length; i++) {
		cy.log("Verifierar att alla loggar på index " + i + " har samma intygsid. Antal loggar på detta index är " + (idSplitArray[i].length - 1))
		for (var j = 1; j < idSplitArray[i].length; j++) {
			expect(idSplitArray[i][0]).to.equal(idSplitArray[i][j].activity.activityLevel);
		}
	}

	for (var i = 0; i < idSplitArray.length; i++) {
		// Hämta alla loggar från mocken
		cy.log("Hämtar loggar för intygsid " + idSplitArray[i][0])

		cy.request({
			method: 'GET',
			url: mockBaseUrl + idSplitArray[i][0]
		}).then((resp) => {
			expect(resp.status).to.equal(200);
			cy.wrap(resp).its('body').then((body) => {
				cy.log("Detta är body från response vid hämtning av loggar från mock:");
				cy.log(body);

				// Loopa igenom alla poster i mocken för detta id och spara i kronologisk ordning.
				// Första logitem kan vi spara direkt för vi vet att det är minst ett sådant
				var logghändelser = []

				// Städa bort alla <br>-taggar och alla blanksteg mellan taggar
				body = body.replace(/<br>/g, "")
				body = body.replace(/>\s+</g, "><");
				cy.log("Body efter att alla <br>-taggar och alla whitespaces är borta.")
				cy.log(body)

				var bodyDoc = document.createElement("div")
				bodyDoc.innerHTML = body
				bodyDoc.children["0"].remove(); // Ta bort headern som skickas med i bodyn

				// getElementsByTagName returnerar en HTMLCollection som behöver sorteras. Inspirerat av
				// https://stackoverflow.com/questions/7059090/using-array-prototype-sort-call-to-sort-a-htmlcollection
				var arr = [].slice.call(bodyDoc.getElementsByTagName("ns2:Log"));
				arr.sort(function(a,b) {
					var datumA = a.getElementsByTagName("startdate")[0].innerText
					var datumB = b.getElementsByTagName("startdate")[0].innerText

					if (datumA < datumB) {
						return -1
					}
					if (datumA > datumB) {
						return 1
					}
					// ToDo: Inträffar detta ibland? I så fall får vi ta ett beslut hur vi ska hantera det. Endera lägga in wait() i testfallen så att händelserna inträffar vid
					// olika tidpunkter alternativt så implementeras en avancerad jämförelsefunktion senare som checkar alla kombinationer av loggar som inträffar vid samma
					// tidpunkt. Går det att öka upplösningen för tidsstämplarna så att ex.vis millisekunder också inkluderas?
					assert.Equal(true, false, 'Sorteringsalgoritmen för logghändelser från mocken hittade två tidsstämplar som är lika. Vi vet därför inte vilken som kom först!')
					return 0
				});

				// debug - ta bort när det funkar
				cy.log("Sorterade arrayen ser ut så här:")
				for(var m = 0; m < arr.length; m++) {
					cy.log("log index " + m + ":")
					cy.log(arr[m].innerHTML);
				}

				// Ursprungliga arrayen med förväntade event innehåller URL på index 0. Skapa ny array utan detta värde
				var förväntadeHändelser = idSplitArray[0].slice(1); // DEBUG! Här ska det vara i men i = 1 av någon anledning! (Asynkron kod?)
				assert.equal(arr.length, förväntadeHändelser.length, "Kontrollerar antal logghändelser");

				// Gå igenom listan med logghändelser och bocka av, en efter en.
				// Utgår från https://inera-certificate.atlassian.net/wiki/spaces/IT/pages/41353325/GE-005+PDL-loggning+i+Webcert
				for (var j = 0; j < förväntadeHändelser.length; j++) {
					// Verifiera att antalet children är rätt, d.v.s att antalet element på högsta nivån stämmer
					assert.equal(arr[j].children.length, 5)

					//---- Element 'Logid' ----//
					var logIdElems = arr[j].getElementsByTagName("logid")
					assert.equal(logIdElems.length, 1, "Kontrollerar antal underelement till 'logid', index " + j);
					// ToDo: Verifierar endast att det finns ett id. Tittar inte på format eller liknande. Räcker det?
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
					var startDate = activity.getElementsByTagName("startdate")[0].innerText
					assert.notEqual(new Date(startDate), "Invalid Date", "Kontrollerar datumstämpel, index " + j);

					// Purpose
					assert.equal(activity.getElementsByTagName("purpose")[0].innerText, "Vård och behandling", "Kontrollerar hårdkodat 'purpose', index " + j);

					//---- Element 'User' ----//
					var user = arr[j].getElementsByTagName("user")[0];
					// userid, name, assignment, title, careprovider, careunit
					// OBS! Username ska inte vara med framöver. Då ska antal underelement minskas med 1.
					assert.equal(user.children.length, 6, "Kontrollerar antal underelement till 'user', index " + j);
					assert.equal(user.getElementsByTagName("userid")[0].innerText, förväntadeHändelser[j].user.userId, "Kontrollerar userid, index " + j);
					// assert.equal(user.getElementsByTagName("name")[0].innerText, förväntadeHändelser[j].user.name, "Kontrollerar name, index " + j); // ToDo: Ska tas bort - när?
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
					/*
					// Verifiera att 'resources' endast har ett underelement, 'resource'
					var resources = arr[j].getElementsByTagName("resources");
					assert.equal(arr[j].getElementsByTagName("resources").children.length, 1, "Kontrollerar antal underelement till 'resources', index " + j);
					var resource = arr[j].getElementsByTagName("resources")[0].getElementsByTagName('resource')[0];
					// Resource har underelement resourcetype, patient, careprovider, careunit
					assert.equal(resource.children.length, 4, "Kontrollerar antal underelement till 'resource', index " + j);
					assert.equal(resource.getElementsByTagName('resourcetype')[0].innerText, "Intyg", "Kontrollerar hårdkodat 'resourcetype', index " + j);

					// Underelement 'Patient'
					var patient = resource.getElementsByTagName('patient')[0];
					assert.equal(patient.children.length, 1, "Kontrollerar antal underelement till 'patient', index " + j);
					assert.equal(patient.getElementsByTagName('patientid')[0].innerText,
								 förväntadeHändelser[j].resources.resource.patient.patientId,
								 "Kontrollerar patientid, index " + j);
					*/
				}
			});
		});
	}
});
