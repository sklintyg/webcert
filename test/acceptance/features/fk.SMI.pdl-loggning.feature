# language: sv

@pdl @smi
Egenskap: PDL-loggning för SMI-intyg

Bakgrund: Jag är inloggad
	Givet att jag är inloggad som läkare
	Och jag går in på en patient

# Första ändring per ändringssession ska loggas
@skapa @skriva
Scenario: Skapa SMI intyg
	När jag går in på att skapa ett slumpat SMI-intyg
	Så ska det nu finnas 1 loggaktivitet "Skriva" för intyget
	Och jag går tillbaka
	Och jag går in på utkastet
	Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
	Och jag ändrar diagnoskod
	Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget

@öppna
Scenario: Öppna SMI-intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten

@signera
Scenario: Signera SMI intyg
	När jag går in på att skapa ett slumpat SMI-intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska loggaktivitet "Signera" skickas till loggtjänsten

@skicka @utskrift
Scenario: Skicka SMI intyg till mottagare
  När jag går in på ett slumpat SMI-intyg med status "Signerat"
  Och jag skickar intyget till Försäkringskassan
  Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

@skriv-ut @utskrift @notReady
Scenario: Skriv ut SMI intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"
	Och jag skriver ut intyget
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

@radera
Scenario: Radera SMI utkast
  När jag går in på att skapa ett slumpat SMI-intyg
	Och jag raderar utkastet
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

@makulera @waitingForFix
Scenario: Makulera SMI intyg
	När  jag går in på ett slumpat SMI-intyg med status "Mottaget"
	Och jag makulerar intyget
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

@kopiera @notReady
Scenario: Kopiera SMI intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"
	Och jag kopierar intyget
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten
	Och ska loggaktivitet "Skriva" skickas till loggtjänsten
