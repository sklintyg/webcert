#language: sv
@AVLIDEN
Egenskap: Avliden patient

@PS-001
Scenario: Varning om patient är avliden
    Givet att jag är inloggad som läkare
    När jag går in på en patient som är avliden
    Så ska jag varnas om att "Patienten är avliden"

@FRÅGA-FRÅN-VÅRDEN @INTEGRATION
Scenario: Avliden Patient - Vården kan ställa frågor på ett intyg
    Givet att jag är inloggad som djupintegrerad läkare
	Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Och jag går in på intyget via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan
	
	Och jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
	Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
	Och Försäkringskassan skickar ett svar
	Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
    Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
	 
@FRÅGA-FRÅN-FK @SMI @INTEGRATION
Scenario: Avliden Patient - Försäkringskassan kan ställa frågor på ett intyg 
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan ställer en "OVRIGT" fråga om intyget
    Och jag svarar på frågan
    Så ska det synas vem som svarat

@SVARA-PA-KOMPLETTERING @INTEGRATION @SMI
Scenario: Avliden Patient - Vården kan svara på komplettering med ett nytt intyg
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
    Och jag går in på intyget via djupintegrationslänk
    Och jag väljer att svara med ett nytt intyg
    Så ska jag se kompletteringsfrågan på utkast-sidan
    Och jag signerar intyget
    Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
