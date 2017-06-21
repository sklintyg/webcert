#language: sv

@infoSaknasIPU
Egenskap: Personnummer ej i PU

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

@PnrEjPU @waitingForFix
Scenario: Jag ska få ett felmeddelande när jag skriver in ett personnummer som inte finns i PU
	När jag anger ett personnummer som inte finns i PUtjänsten
	Så ska ett fel-meddelande visa "Personnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt."

# Om PU tjänsten är ligger nere
@patientSaknarNamn
Scenario: Jag ska få ett felmeddelande när jag skriver in ett personnummer som saknar namn i PU
	När jag går in på en patient som saknar namn i PU-tjänsten
	Så ska ett felmeddelande visas som innehåller texten "Namn för det nummer du har angett kunde tyvärr inte hämtas från folkbokföringsregistret"
