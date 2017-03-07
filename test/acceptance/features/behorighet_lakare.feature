# language: sv
@behorighet @lakare @hurr
Egenskap: Behörigheter för en läkare

Scenario: Kan endast nå intyg på inloggad vårdenhet
	Givet att jag är inloggad som läkare utan angiven vårdenhet
	Så ska jag se en rubrik med texten "Välj din vårdenhet"

	När jag väljer vårdenheten "TSTNMT2321000156-102R"
	Så ska jag se en rubrik med texten "Sök/skriv intyg"

	När jag går in på en patient
	Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"


Scenario: Kan se intyg på underenheter när jag loggar in på överliggande enhet
	Givet att jag är inloggad som läkare på underenhet "TSTNMT2321000156-107J"
	När jag går in på en patient
	Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-1003"
	Och går till den sparade länken
	Så ska intygets status vara "Intyget är signerat"

@skriv-ut @annan-enhet
Scenario: Kan inte se intyg på överliggande enhet när jag är inne på underenhet
	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-1003"
	När jag går in på en patient
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg

	Så ska det finnas en knapp för att skriva ut utkastet
	
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på underenhet "TSTNMT2321000156-107J" och inte har uppdrag på "TSTNMT2321000156-1003"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"

	@fornya-utkast 
Scenario: Det går att förnya signerade och mottagna intyg från intygslistan men inte utkast
	Givet att jag är inloggad som läkare
	Och jag går in på en patient
	Så ska Förnya-knappen visas för alla signerade eller mottagna "Läkarintyg FK 7263"-intyg

   	Givet att det finns intygsutkast
   	Så ska Förnya-knappen inte visas för något utkast
