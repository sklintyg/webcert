// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

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

Cypress.Commands.add("loginArnoldOchGaTillValjPatient", () => {
	cy.loginArnoldNormal().then(() => {
		// Hur gör man automatisk redirect så som GUI:t gör?
		cy.visit('/#/create/choose-patient/index');
	})
})

Cypress.Commands.add("goToCreateCertForTolvanAsArnold", () => {
	cy.loginArnoldNormal().then(() => {
		cy.visit('/#/create/choose-intyg-type/19121212-1212/index');
	});
});

/*
Cypress.Commands.add("createLisjpDraft", () => {
	// Kräver att läkare är inloggad

});
*/
