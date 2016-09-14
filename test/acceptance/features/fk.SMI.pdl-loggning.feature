# language: sv

@pdl @smi
Egenskap: PDL-loggning för SMI-intyg

Bakgrund: Jag är inloggad
	Givet att jag är inloggad som läkare
	Och jag går in på en patient

# Första ändring per ändringssession ska loggas
@skapa @skriva
Scenariomall: Skapa <intygKod> intyg 
	När jag går in på att skapa ett <intyg> intyg
	Så ska det nu finnas 1 loggaktivitet "Skriva" för intyget
	Och jag går tillbaka
	Och jag går in på utkastet
	Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
	Och jag ändrar diagnoskod
	Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget

Exempel:
  |intygKod | 	intyg 								| 
  |LUSE		|  	"Läkarutlåtande för sjukersättning" | 
  |LISU		| 	"Läkarintyg för sjukpenning utökat" | 

@öppna @notReady
Scenariomall: Öppna <intygKod>-intyg
	När jag går in på ett <intyg> med status "Signerat"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten

Exempel:
  |intygKod | 	intyg 								| 
  |LUSE		|  	"Läkarutlåtande för sjukersättning" | 
  |LISU		| 	"Läkarintyg för sjukpenning utökat" | 


@signera
Scenariomall: Signera <intygtyp> intyg
	När jag går in på att skapa ett <intyg> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska loggaktivitet "Signera" skickas till loggtjänsten

Exempel:
  |intygKod | 	intyg 								| 
  |LUSE		|  	"Läkarutlåtande för sjukersättning" | 
  |LISU		| 	"Läkarintyg för sjukpenning utökat" | 

# @skicka @utskrift
# Scenario: Skicka intyg till mottagare
#     När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
# 	Och jag skickar intyget till Försäkringskassan
# 	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

# @skriv-ut @utskrift
# Scenario: Skriv ut intyg
#     När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
# 	Och jag skriver ut intyget
# 	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten

# @radera
# Scenario: Radera utkast
#     När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
# 	Och jag raderar utkastet
# 	Så ska loggaktivitet "Radera" skickas till loggtjänsten

# @makulera
# Scenario: Makulera intyg
# 	När  jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
# 	Och jag makulerar intyget
# 	Så ska loggaktivitet "Radera" skickas till loggtjänsten

# @kopiera
# Scenario: Kopiera intyg
# 	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
# 	Och jag kopierar intyget
# 	Så ska loggaktivitet "Läsa" skickas till loggtjänsten
# 	Och ska loggaktivitet "Skriva" skickas till loggtjänsten
