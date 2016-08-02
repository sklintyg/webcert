# language: sv
@behorighet @vardadmin 
Egenskap: Behörigheter för en vårdadministratör

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	Och jag går in på en patient

Scenario: Kan kopiera Läkarintyg FK 7263
   När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen tillgänglig

Scenario: Kan kopiera Transportstyrelsens läkarintyg 
   När jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så är kopieraknappen tillgänglig

Scenario: Kan kopiera Transportstyrelsens läkarintyg, diabetes intyg
   När jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så är kopieraknappen tillgänglig

Scenario: Kan inte signera Läkarintyg FK 7263
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

Scenario: Kan inte signera Transportstyrelsens läkarintyg
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

@diabetes
Scenario: Kan inte signera Transportstyrelsens läkarintyg, diabetes
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig



