#language: sv

@ersatt
Egenskap: Ersätta intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
    När jag går in på en patient

@ersatta-btn @ersatta-text @avbryt
Scenario: Kan ersätta signerade intyg
	När jag går in på att skapa ett slumpat intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska jag se en knapp med texten "Ersätt"

	När jag klickar på ersätta knappen
	Så ska meddelandet som visas innehålla texten "Ett intyg kan ersättas om det innehåller felaktiga uppgifter eller om ny information tillkommit efter att intyget utfärdades."

@ersatt-intyg-lank
Scenario: När man ersatt intyg så ska informationstext på ett ersatt intyg finnas
	När jag går in på att skapa ett slumpat intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag klickar på ersätta knappen och ersätter intyget
	Och jag går tillbaka till det ersatta intyget
	Så ska jag se en texten "Intyget har ersatts av detta intyg" som innehåller en länk till det ersatta intyget

@ersatt-intyg-buttons
Scenario: När man ersatt ett intyg så ska det ersatta intyg inte gå att skicka,kopiera,ersätta,förnya
	När jag går in på att skapa ett slumpat intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag klickar på ersätta knappen och ersätter intyget
	Och jag går tillbaka till det ersatta intyget
	Så ska det inte finnas knappar för "skicka,kopiera,ersätta,förnya"
