# language: sv
@SEKRETESSMARKERING @VARNINGMEDDELANDEN @GE-008 @PS-002
Egenskap: GE-008 - Sekretessmarkerad patient

Bakgrund:
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med sekretessmarkering

@LISJP @FORNYA @INTYGSTJÄNSTEN
Scenario: PS-002 - Varningmeddelanden när man går in på patient
   När jag skickar ett "Läkarintyg för sjukpenning" intyg till Intygstjänsten
   Så ska det finnas en knapp med texten "Förnya"
   Så ska jag varnas om att "Patienten har en sekretessmarkering"

@SIGNERA @FRÅGASVAR @SMI @SKRIV-UT @MO-007 @SE-011
Scenario: SE-011 - Varningmeddelanden i fråga-svar för sekretessmarkerad person
	Så ska jag varnas om att "Patienten har en sekretessmarkering"
	När jag går in på att skapa ett slumpat SMI-intyg
	Så ska jag varnas om att "Patienten har en sekretessmarkering"

	När jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska jag varnas om att "Patienten har en sekretessmarkering"

	#MO-007
	Och jag skriver ut intyget
	Så ska jag varnas om att "OBS! Patienten har en sekretessmarkering" i nytt fönster

	När jag skickar intyget till Försäkringskassan
	Och Försäkringskassan ställer en "OVRIGT" fråga om intyget
	Och jag går till sidan Frågor och svar
	
	Så ska frågan ha en indikator som indikerar sekretessmarkering

@UTKAST @SMI @SE-011
Scenario: SE-011 - Varningmeddelanden i Ej signerade utkast för sekretessmarkerad person
	Så ska jag varnas om att "Patienten har en sekretessmarkering"
	När jag går in på att skapa ett slumpat SMI-intyg
	Så ska jag varnas om att "Patienten har en sekretessmarkering"

	Och jag går till ej signerade utkast
	
	Så ska intyget ha en indikator som indikerar sekretessmarkering
