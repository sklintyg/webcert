# language: sv
@notReady
Egenskap: Behörighet i Webcert

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som uthoppsläkare

@fråga-uthopp @kristi
Scenario: Ska gå in på intyg som är skickat till intygstjänsten
När jag skickar ett intyg till Intygstjänsten
Och jag skickar intyget direkt till Försäkringskassan
Och Försäkringskassan ställer en "Kontakt" fråga om intyget 
Så ska jag få ett mejl med ämnet "Försäkringskassan har ställt en fråga angående ett intyg"
