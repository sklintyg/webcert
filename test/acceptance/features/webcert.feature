# language: sv
@smoke @webcert
Egenskap: Kontrollera att webcerts olika funktioner går att använda

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare "Jan Nilsson"

@RegisterMedicalCertificate @minaintyg 
Scenario: Skapa och signera ett intyg i webcert
	När jag väljer patienten "19520617-2339"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och fyller i alla nödvändiga fält för intyget
	Och signerar intyget
	Så ska "Läkarintyg FK 7263"-intygets status vara "Intyget är signerat"

#	När jag går till Mina intyg för patienten "19520617-2339"
#	Så ska intyget finnas i Mina intyg

@SendMedicalCertificate @minaintyg 
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	Givet att ett intyg är skapat
	När jag öppnar intyget
	Och intyget är signerat
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och mottaget av Försäkringskassans system."

#	När jag går till mvk på patienten "19520617-2339"
#	Så ska intygets status i mvk visa "Mottaget av Försäkringskassans system"

@RevokeMedicalCertificate @notReady
Scenario: Makulera ett skickat intyg
	Givet att ett intyg är skapat
	När jag öppnar intyget
	Och intyget är skickat till försäkringskassan
	Och jag makulerar intyget
	Så ska jag få en dialogruta som säger "Kvittens - Återtaget intyg"
	Så ska intyget visa varningen "Intyget är makulerat"

	När jag går till mvk på patienten "19520617-2339"
	Så ska intygets status i mvk visa "Makulerat"

@arkivera @notReady
Scenario: Arkivera ett intyg i mvk
	Givet att ett intyg är skapat
	När jag går till mvk på patienten "19520617-2339"
	Och jag arkiverar intyget i mvk
	Så ska intygets inte visas i mvk


