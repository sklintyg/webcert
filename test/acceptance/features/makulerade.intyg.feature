# language: sv

@fornya-knapp
Egenskap: Makulerat intyg ska kunna förnyas

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@fk7263-förnya-knapp @LegacyFk7263
Scenario: Det ska inte gå att förnya ett makulerat FK7263 intyg
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	#När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Och jag makulerar intyget
	Så ska det inte finnas en knapp med texten "Förnya"

@SMI-förnya-knapp
Scenario: Det ska inte gå att förnya makulerat SMI-intyg
	När jag går in på ett slumpat SMI-intyg med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"
	Så ska det inte finnas en knapp med texten "Förnya"

@upplys-om-makulering
Scenario: Användaren ska upplysas om att intyget makulerats
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
    Och jag går in på utkastet 
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"

Scenario: Användaren ska kunna skriva ut ett makulerat intyg
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
    Och jag går in på utkastet 
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

	Och jag makulerar intyget
	Så ska det finnas en knapp med texten "Skriv ut"

@notReady
Scenario: Ersatt intyg vid makulering ska innehålla uppdaterade personuppgifter
	När jag skickar ett intyg med ändrade personuppgifter till Intygstjänsten
	Och jag går in på intyget
	Och jag makulerar intyget och ersätter med nytt intyg
	Så ska intyget inte innehålla gamla personuppgifter
