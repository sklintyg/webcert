# language: sv

@uthopp @uthoppsläge
Egenskap: Uthoppsläge Fk7263

@fråga-uthopp @mailnotifikation
Scenario: Mail när fråga på intyg inkommer
	Givet att jag är inloggad som uthoppsläkare
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och Försäkringskassan ställer en "Kontakt" fråga om intyget
	Så ska jag få ett mejl med ämnet "Försäkringskassan har ställt en fråga angående ett intyg"

@fråga-till-fk
Scenario: Skicka fråga till Försäkringskassan
	Givet att jag är inloggad som uthoppsläkare
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och jag går in på intyget via uthoppslänk
	Och jag skickar en fråga med ämnet "Arbetstidsförläggning" till Försäkringskassan
	Så ska ett info-meddelande visa "Frågan är skickad till Försäkringskassan"
	Och ska jag se min fråga under ohanterade frågor

@byt-flik
Scenario:Varning när jag lämnar fråga/svar vy
	Givet att jag är inloggad i uthoppsläge
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och jag går in på intyget via uthoppslänk
	Och går in på Sök/skriv intyg
	Så ska jag få en dialog med texten "Observera att intyg ska utfärdas via journalsystemet och inte via Webcert."
