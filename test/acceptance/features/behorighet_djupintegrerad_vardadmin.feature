# language: sv
@behorighet @djupintegration @vårdadmin
Egenskap: Behörigheter för en djupintegrerad vårdadministratör

Bakgrund: Logga in
	Givet att jag är inloggad som djupintegrerad vårdadministratör

@signera
Scenario: Kan inte signera
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
	Så visas inte signera knappen