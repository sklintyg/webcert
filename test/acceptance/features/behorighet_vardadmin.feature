# language: sv
@behorighet
Egenskap: Behörigheter för en vårdadministratör

# @vardadmin
Scenario: Kan kopiera Läkarintyg FK 7263 
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen tillgänglig

#@vardadmin
Scenario: Kan kopiera Transportstyrelsens läkarintyg 
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så är kopieraknappen tillgänglig

@vardadmin 
Scenario: Kan kopiera Transportstyrelsens läkarintyg, diabetes intyg
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så är kopieraknappen tillgänglig

#@vardadmin
Scenario: Kan inte signera intyg som en vårdadministratör, Läkarintyg FK 7263
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

@vardadmin
Scenario: Kan inte signera Transportstyrelsens läkarintyg
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

@vardadmin
Scenario: Kan inte signera Transportstyrelsens läkarintyg, diabetes
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig



