# language: sv

@behorighet @vardadmin
Egenskap: Behörigheter för en uthoppad vårdadministratör

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på en patient

@vardadmin_byte
Scenario: Kan byta vårdenhet 
   När jag byter vårdenhet till "TSTNMT2321000156-106N"
   Så vårdenhet ska vara "nmt_vg1 - Smultronet"

@vardadmin_vidarebefordra
Scenario: Kan vidarebefodra ett utkast

	Och går in på Ej signerade utkast 
	Så visas Vidarebefodra knappen

@vardadmin_ej_signera
Scenario: Kan ej signera intyg.
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så visas inte signera knappen

@vardadmin_personuppgifter
Scenario: Kan visa "Hämta personuppgifter"-knapp på utkast
		Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
		Så visas Hämta personuppgifter knappen
		

@vardadmin_sekretessmarkering
Scenario: Kan visa information om sekretessmarkerade personuppgifter
		Och jag går in på en patient med sekretessmarkering
		Så ska en varningsruta innehålla texten "Patienten har en sekretessmarkering."
