# language: sv
@BEHÖRIGHET @DJUPINTEGRATION @LAKARE
# PRIVILEGE_NAVIGERING
Egenskap: Behörigheter för en djupintegrerad läkare

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och jag går in på en patient

@MAKULERA @LISJP @WC-F019
Scenario: Kan makulera sjukintyg
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"

@FÖRNYA @LISJP @UTSKRIFT
Scenario: Kan förnya och signera ett läkarintyg
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
   Så ska det finnas en knapp för att förnya intyget
   
   Och ska det finnas en knapp för att skriva ut intyget

@UTSKRIFT @SIGNERA @TS
Scenario: Signera ett ts-intyg
   När att vårdsystemet skapat ett intygsutkast för slumpat TS-intyg
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag fyller i alla nödvändiga fält för intyget
   Så ska det finnas en knapp för att skriva ut utkastet

   När jag signerar intyget
   Så ska det finnas en knapp för att skriva ut intyget


@KOMPLETTERING @SVARA @LISJP @FRÅGA-FRÅN-FK
Scenario: Besvara kompletteringsfråga
   När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"
   Och Försäkringskassan ställer en "KONTKT" fråga om intyget
   Och jag svarar på frågan
   Så kan jag se mitt svar i högerfältet

@KOMPLETTERING @SVARA @LISJP
Scenario: Svara med nytt intyg
   När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"
   Och Försäkringskassan ställer en "KOMPLT" fråga om intyget

   När jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar och skickar kompletteringen
   Och ska intygets första status vara "Intyget är skickat till Försäkringskassan"

  
