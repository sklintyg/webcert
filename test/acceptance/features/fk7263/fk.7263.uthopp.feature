# language: sv

@uthopp @uthoppsläge @NOTREADY @DEPRICATED @MOVE_TO_SMI
Egenskap: FK7263 - Uthoppsläge

@fråga-uthopp @mailnotifikation @notReady
Scenario: Mail när fråga på intyg inkommer
	#Om testfallet felar kolla : SELECT * FROM INTEGRERADE_VARDENHETER WHERE ENHETS_ID = 'TSTNMT2321000156-107Q';
	#eftersom att vi måste skapa fk7263 intyg via RegisterMedicalCertificate (som vi gör på denna enheten blir den loggad som integrerad)
	Givet att jag är inloggad som uthoppsläkare
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och Försäkringskassan ställer en "KONTKT" fråga om intyget
	Så ska jag få ett mejl med ämnet "Försäkringskassan har ställt en fråga angående ett intyg"

@fråga-till-fk
Scenario: Skicka fråga till Försäkringskassan
	Givet att jag är inloggad som uthoppsläkare
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och jag går in på intyget via uthoppslänk
	Och jag skickar en fråga med ämnet "Arbetstidsförläggning" till Försäkringskassan
	Och ska jag se min fråga som ohanterad

@byt-flik
Scenario:Varning när jag lämnar fråga/svar vy
	Givet att jag är inloggad i uthoppsläge
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och jag går in på intyget via uthoppslänk
	Och går in på Sök/skriv intyg
	Så ska jag få en dialog med texten "Observera att intyg ska utfärdas via journalsystemet och inte via Webcert."
