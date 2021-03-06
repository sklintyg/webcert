#language: sv

@SAMORDNINGSNUMMER
Egenskap: Samordningsnummer

Bakgrund: Jag befinner mig på webcerts förstasida

@SMI
Scenario: Det ska gå att öppna ett signerat SMI-intyg i Mina intyg för en patient med samordningsnummer
	Givet att jag är inloggad som läkare
	När jag går in på en patient med samordningsnummer
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets första status vara "Intyget är signerat"
	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg
	
@CREATEDRAFT
Scenario: Det ska gå att skicka CreateDraft:2 med ett samordningsnummer
  Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
  Och att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning" med samordningsnummer
  Så jag går in på intygsutkastet via djupintegrationslänk

@PNR-EJ-I-PU
Scenario: Jag ska få ett felmeddelande när ett samordningsnummer saknas i PUtjänsten
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	När jag anger ett samordningsnummer som inte finns i PUtjänsten
	Så ska jag varnas om att "Samordningsnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt."