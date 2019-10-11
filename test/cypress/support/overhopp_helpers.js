export function verifyPatStatus(arg) {
    switch(arg) {
        case "utanParameter":
            // Ska ej synas
            cy.contains('Patienten är avliden').should('not.exist');
            cy.get('#intyg-djupintegration-name-changed').should('not.exist');
            cy.contains('Patientens namn skiljer sig från det i journalsystemet').should('not.exist');
            cy.get('#wc-new-person-id-message-text').should('not.exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer:').should('not.exist');
            cy.contains('Patientens personnummer har ändrats').should('not.exist');
            cy.get('#saknarAdress').should('not.exist');
            break;
        case "avliden":
            // Ska ej synas
            cy.get('#intyg-djupintegration-name-changed').should('not.exist');
            cy.contains('Patientens namn skiljer sig från det i journalsystemet').should('not.exist');
            cy.get('#wc-new-person-id-message-text').should('not.exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer:').should('not.exist');
            cy.contains('Patienten är avliden').should('not.exist');
            break;
        case "patientNamn":
            // Ska ej synas
            cy.contains('Patienten är avliden').should('not.exist');
            cy.get('#wc-new-person-id-message-text').should('not.exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer:').should('not.exist');
            cy.contains('Patientens personnummer har ändrats').should('not.exist');
            cy.get('#saknarAdress').should('not.exist');
            // Ska synas
            cy.get('#intyg-djupintegration-name-changed').should('exist');
            cy.contains('Patientens namn skiljer sig från det i journalsystemet').should('exist');
            cy.get('#intyg-djupintegration-name-changed').click();
            cy.contains('Patientens namn skiljer sig').should('exist');
            cy.contains('Patientens namn som visas i intyget har hämtats från Personuppgiftstjänsten och skiljer sig från det som är lagrat i journalsystemet.').should('exist');
            cy.get('#confirmationOkButton').click();
            break;
        case "reservnummer":
            // Ska ej synas
            cy.contains('Patienten är avliden').should('not.exist');
            cy.get('#intyg-djupintegration-name-changed').should('not.exist');
            cy.get('#saknarAdress').should('not.exist');
            cy.contains('Patientens personnummer har ändrats').should('not.exist');
            // Ska synas
            cy.get('#wc-new-person-id-message-text').should('exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer: 19270926308A.').should('exist');
            cy.wait(1000);
            cy.get('#wc-new-person-id-message-text').click();
            cy.contains('Patientens samordningsnummer').should('exist');
            cy.contains('Om ett intyg skapas utifrån detta intyg kommer det nya intyget skrivas på samordningsnumret.').should('exist');
            cy.get('#confirmationOkButton').click();
            break;
        case "ändratPnr":
            // Ska ej synas
            cy.contains('Patienten är avliden').should('not.exist');
            cy.get('#intyg-djupintegration-name-changed').should('not.exist');
            cy.get('#saknarAdress').should('not.exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer:').should('not.exist');
            // Ska synas
            cy.get('#wc-new-person-id-message-text').should('exist');
            cy.contains('Patientens personnummer har ändrats').should('exist');
            break;
        case "adressSaknasTSBAS":
            // Ska ej synas
            cy.contains('Patienten är avliden').should('not.exist');
            cy.get('#intyg-djupintegration-name-changed').should('not.exist');
            cy.get('#wc-new-person-id-message-text').should('not.exist');
            cy.contains('Patienten har samordningsnummer kopplat till reservnummer:').should('not.exist');
            cy.contains('Patientens personnummer har ändrats').should('not.exist');
            // Ska synas
            cy.get('#saknarAdress').should('exist');
            cy.get('#saknarAdress').click();
            cy.contains('Ingen adress angavs av journalsystemet').should('exist');
            cy.contains('Journalsystemet angav inga adressuppgifter för patienten, därför har en slagning i den nationella personuppgiftstjänsten genomförts. Det är adressen som finns registrerad där som nu visas i intyget.').should('exist');
            cy.get('#confirmationOkButton').click();

    };
};