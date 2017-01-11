# language: sv
@behorighet @lakare @hurr
Egenskap: Behörigheter för en läkare

@testtest
Scenario: Kan endast nå intyg på inloggad vårdenhet
	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-102R"
	När jag går in på en patient
	Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-1004"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"


Scenario: Kan se intyg på underenheter när jag loggar in på överliggande enhet
	Givet att jag är inloggad som läkare på underenhet "TSTNMT2321000156-107J"
	När jag går in på en patient
	Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-1003"
	Och går till den sparade länken
	Så ska intygets status vara "Intyget är signerat"

Scenario: Kan inte se intyg på överliggande enhet när jag är inne på underenhet
	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-1003"
	När jag går in på en patient
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på underenhet "TSTNMT2321000156-107J" och inte har uppdrag på "TSTNMT2321000156-1003"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"
