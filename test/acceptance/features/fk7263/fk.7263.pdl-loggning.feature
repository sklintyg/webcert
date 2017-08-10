# language: sv

@pdl @fk7263
Egenskap: PDL-loggning för FK7263
#Saknas: TF för läsning av intyg på annan VE

Bakgrund: Jag är inloggad
	Givet att jag är inloggad som läkare
	Och jag går in på en patient

# Första ändring per ändringssession ska loggas
@skapa @skriva
Scenario: Skapa intyg
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag går tillbaka
	Och jag går in på utkastet
	Och jag ändrar diagnoskod
	Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget

@öppna
Scenario: Öppna intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten

@signera
Scenario: Signera intyg
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska loggaktivitet "Signera" skickas till loggtjänsten

@skicka @utskrift
Scenario: Skicka intyg till mottagare
    När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

@skriv-ut @utskrift
Scenario: Skriv ut intyg
    När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skriver ut intyget
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

@radera
Scenario: Radera utkast
    När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag raderar utkastet
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

@makulera
Scenario: Makulera intyg
	När  jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
	Och jag makulerar intyget
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

@fornya
Scenario: förnya intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten
	Och jag förnyar intyget
	Och jag anger datum för Baserat på
	Och jag anger datum för arbetsförmåga
	Och jag anger kontakt med FK
	Och jag signerar intyget
	Och ska loggaktivitet "Skriva" skickas till loggtjänsten
