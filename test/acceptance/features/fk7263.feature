# language: sv
@smoke @fk7263
Egenskap: Hantera FK7263-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

@RegisterMedicalCertificate @minaintyg @keepIntyg
Scenario: Skapa och signera ett intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"	
	När jag går till Mina intyg för patienten "19971019-2387"
	Så ska intyget finnas i Mina intyg
	
@SendMedicalCertificate @minaintyg @keepIntyg
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag väljer patienten "19971019-2387"
    Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system."
	När jag går till Mina intyg för patienten "19971019-2387"
	Så ska intygets status i Mina intyg visa "Mottaget av Försäkringskassans system"

@RevokeMedicalCertificate
Scenario: Makulera ett skickat intyg
	När jag väljer patienten "19971019-2387"
    Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"
	När jag går till Mina intyg för patienten "19971019-2387"
	Så ska intygets status i Mina intyg visa "Makulerat"

Scenario: Kopiera ett signerat intyg
	När jag väljer patienten "19971019-2387"
    Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag kopierar intyget
	Och signerar intyget
    Så ska intygets status vara "Intyget är signerat"	
