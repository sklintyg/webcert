#language: sv

@ersatt
Egenskap: Ersätta intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
    När jag går in på en patient

@ersatt-intyg-lank @ersatta-text
Scenario: När man ersatt intyg så ska informationstext på ett ersatt intyg finnas
	När jag går in på att skapa ett slumpat intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag klickar på ersätta knappen 
	Så ska meddelandet som visas innehålla texten "Ett intyg kan ersättas om det innehåller felaktiga uppgifter eller om ny information tillkommit efter att intyget utfärdades."

	När jag klickar på ersätt-knappen i dialogen
	Och jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
	Och jag signerar intyget

	Och jag går tillbaka till det ersatta intyget
	Så ska intygets första status vara "Intyget har ersatts av detta intyg"

@ersatt-intyg-buttons
Scenario: När man ersatt ett intyg så ska det ersatta intyg inte gå att skicka,ersätta,förnya
	När jag går in på att skapa ett slumpat intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag klickar på ersätta knappen
	Och jag klickar på ersätt-knappen i dialogen
	Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan
	Och jag går tillbaka till det ersatta intyget
	Så ska det inte finnas knappar för "skicka,ersätta,förnya"
