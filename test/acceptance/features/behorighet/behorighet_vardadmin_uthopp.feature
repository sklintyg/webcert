# language: sv

@BEHÖRIGHET @VÅRDADMIN @UTHOPP
Egenskap: Behörigheter för en uthoppad vårdadministratör

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som uthoppad vårdadministratör
	Och jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten

@BYTA-VÅRDENHET
Scenario: Kan byta vårdenhet 
   När jag går in på intyget via uthoppslänk
   Och jag byter vårdenhet till "TSTNMT2321000156-106N"
   Så vårdenhet ska vara "Smultronet"
