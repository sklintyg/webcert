#language: sv

@samordningsnummer
Egenskap: Samordningsnummer

Bakgrund: Jag befinner mig på webcerts förstasida

@fk7263
Scenario: Det ska gå att öppna ett signerat FK7263 intyg i Mina intyg för en patient med samordningsnummer
	Givet att jag är inloggad som läkare
	När jag går in på en patient med samordningsnummer
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"
	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

@createDraft1
Scenario: Det ska gå att skicka CreateDraft:1 med ett samordningsnummer
	Givet att jag är inloggad som djupintegrerad läkare
	Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" med samordningsnummer
	Så jag går in på intygsutkastet via djupintegrationslänk

@createDraft2
Scenario: Det ska gå att skicka CreateDraft:2 med ett samordningsnummer
  Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
  Och att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning" med samordningsnummer
  Så jag går in på intygsutkastet via djupintegrationslänk

@saknatsamordningsnummer
Scenario: Jag ska få ett felmeddelande när ett samordningsnummer saknas i PUtjänsten
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
	När jag matar in personnummer som inte finns i PUtjänsten
	Så ska valideringsfelet "Samordningsnumret du har angivit finns inte i folkbokföringsregistret" visas

