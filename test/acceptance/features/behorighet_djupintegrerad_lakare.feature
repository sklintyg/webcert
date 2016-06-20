# language: sv
@behorighet @djupintegration @lakare
# PRIVILEGE_NAVIGERING
Egenskap: Behörigheter för en djupintegrerad läkare

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som djupintegrerad läkare
	När jag går in på en patient

Scenario: Kan makulera sjukintyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"

Scenario: Kan kopiera och signera ett (kopierat)intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag kopierar intyget
	Och jag signerar intyget
	Så ska intygets status vara "Intyget är signerat"

@notReady
Scenario: Kan kopiera ett intyg från tidigare intyg listan (utan att gå in i intyget)
	När kopierar ett signerat intyg
	Så ska intygets status vara "Intyget är signerat"

@notReady
Scenario: Besvara kompleteringsfråga
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan ställer en "Kontakt" fråga om intyget
   Och jag svarar på frågan
   Så ska frågan vara hanterad

@notReady
Scenario: Svara med nytt intyg
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget

   När jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar intyget
   Och jag skickar intyget till Försäkringskassan
   Och ska intygets status vara "Intyget är signerat"
