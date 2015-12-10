# language: sv
@smoke @webcert
Egenskap: Kontrollera att webcerts olika funktioner går att använda

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare "Jan Nilsson"

@RegisterMedicalCertificate @minaintyg @dev
Scenario: Skapa och signera ett intyg i webcert
	När jag väljer patienten "19520617-2339"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och fyller i alla nödvändiga fält för intyget
	Och signerar intyget
	Så ska intygets status vara "Intyget är signerat"

#	När jag går till Mina intyg för patienten "19520617-2339"
#	Så ska intyget finnas i Mina intyg
@SendMedicalCertificate @minaintyg
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag väljer patienten "19520617-2339"
    Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat" 
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system."

	När jag går till Mina intyg för patienten "19520617-2339"
	Så ska intygets status i Mina intyg visa "Mottaget av Försäkringskassans system"

@RevokeMedicalCertificate
Scenario: Makulera ett skickat intyg 
	När jag väljer patienten "19520617-2339"
    Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"

	När jag går till Mina intyg för patienten "19520617-2339"
	Så ska intygets status i Mina intyg visa "Makulerat"

