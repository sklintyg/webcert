# language: sv
@behorighet @djupintegration @vårdadmin
Egenskap: Behörigheter för en djupintegrerad vårdadministratör

Bakgrund: Logga in
	Givet att jag är inloggad som djupintegrerad vårdadministratör

@signera @skriv-ut 
Scenario: Kan inte signera
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget

    Så ska det finnas en knapp för att skriva ut utkastet
    Och ska ett info-meddelande visa "Endast läkare får signera intyget."
	Så visas inte signera knappen
		

# @vardadmin_sekretessmarkering
# Scenario: Kan visa information om sekretessmarkerade personuppgifter
# 		Och jag går in på en patient med sekretessmarkering
# 		Så ska en varningsruta innehålla texten "Patienten har en sekretessmarkering."
