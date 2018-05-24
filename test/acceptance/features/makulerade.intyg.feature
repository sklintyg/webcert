# language: sv

@FÖRNYA @MAKULERAT
Egenskap: Makulerat intyg ska kunna förnyas

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@SMI
Scenario: Det ska inte gå att förnya makulerat SMI-intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"
	Så ska det inte finnas en knapp med texten "Förnya"

@SMI
Scenario: Användaren ska kunna skriva ut ett makulerat intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"

	Och jag makulerar intyget
	Så ska det finnas en knapp med texten "Skriv ut"

@NOTREADY
Scenario: Ersatt intyg vid makulering ska innehålla uppdaterade personuppgifter
	När jag skickar ett intyg med ändrade personuppgifter till Intygstjänsten
	Och jag går in på intyget
	Och jag makulerar intyget och ersätter med nytt intyg
	Så ska intyget inte innehålla gamla personuppgifter
