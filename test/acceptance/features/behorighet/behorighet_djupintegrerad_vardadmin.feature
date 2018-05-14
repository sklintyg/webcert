# language: sv
@behorighet @djupintegration @vårdadmin
Egenskap: Behörigheter för en djupintegrerad vårdadministratör

Bakgrund: Logga in
	Givet att jag är inloggad som djupintegrerad vårdadministratör

@signera @skriv-ut @klar-för-signering
Scenario: Kan markera som klart för signering men inte signera
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget

    Så ska det finnas en knapp för att skriva ut utkastet
	Så visas inte signera knappen

	När jag markerar intyget som klart för signering
	Så ska statusuppdatering "KFSIGN" skickas till vårdsystemet. Totalt: "1"

	När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret visa intyghändelse "KFSIGN"

    När laddar om sidan
	
	Så ska jag se KFSIGN infotexten "Utkastet är sparat och markerat klart för signering."

@RESPONSIBLEHOSPNAME
Scenario: [responsibleHospName] - Vårdadmin ska se signerande läkare
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intyget via djupintegrationslänk och har parametern "responsibleHospName" satt till "Peter Parameter"
	Så ska jag se signerande läkare "Peter Parameter"		
	
	
@signera @klar-för-signering-sparat @LegacyFK7263
Scenario: Kan inte signera FK7263-utkast
	När att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget

    Så ska det finnas en knapp för att skriva ut utkastet
	Så visas inte signera knappen