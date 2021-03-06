# language: sv
@HSA @NEGATIVA
Egenskap: Inloggning med felaktiga HSAuppgifter

@INLOGGNING
Scenariomall: Loggar in som användare trots felaktig hsa data
	När jag loggar in med felaktig uppgift om <felaktighet> i HSAkatalogen
	Så ska jag vara inloggad som 'Läkare'

Exempel:
  |felaktighet			|
  |telefonuppgift		|
  |befattning	  		|
  |adress			    |
  

@VÅRDADMIN
Scenario: En vårdadministratör ska kunna skapa intygsutkast med CreateDraft
	Givet att jag är inloggad som djupintegrerad vårdadministratör
	Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Så jag går in på intygsutkastet via djupintegrationslänk

@SAKNAR_MEDARBETARUPPDRAG
Scenario: En läkare utan giltigt medarbetaruppdrag försöker logga in
	Givet att jag loggar in som läkare utan medarbetaruppdrag
	Så ska ett fel-meddelande visa "Det krävs minst ett giltigt medarbetaruppdrag med ändamål 'Vård och behandling' för att använda Webcert."
