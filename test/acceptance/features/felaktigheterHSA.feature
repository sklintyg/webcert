# language: sv
@hsa @negativa
Egenskap: Inloggning med felaktiga HSAuppgifter

@inloggning
Scenariomall: Loggar in som användare trots felaktig hsa data
	När jag loggar in med felaktig uppgift om <felaktighet> i HSAkatalogen
	Så ska jag vara inloggad som 'Läkare'

Exempel:
  |felaktighet			|
  |telefonuppgift		|
  |befattning	  		|
  |adress			    |
  

@adminstratör @skapa-utkast
Scenario:En vårdadministratör ska kunna skapa intygsutkast med CreateDraft:2
Givet att jag är inloggad som uthoppad vårdadministratör
Och att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
Så jag går in på intygsutkastet via djupintegrationslänk


@saknar_medarbetaruppdrag
Scenario:En läkare utan giltigt medarbetaruppdrag försöker logga in
	Givet att jag loggar in som läkare utan medarbetaruppdrag
	Så ska ett fel-meddelande visa "Det krävs minst ett giltigt medarbetaruppdrag med ändamål 'Vård och behandling' för att använda Webcert."
