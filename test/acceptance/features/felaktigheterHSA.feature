# language: sv
@hsa @negativa
Egenskap: Inloggning med felaktiga HSA luppgifter

Bakgrund: Jag befinner mig på webcerts förstasida


# Det ska vara möjligt att logga in trots felaktiga uppgifter i HSA
@inloggning
Scenariomall: Loggar in som användare trots felaktig hsa data
	När jag loggar in med felaktig uppgift om <felaktighet> i HSAkatalogen
	Så ska jag vara inloggad som 'Läkare'

Exempel:
  |felaktighet			|
  |telefonuppgift		|
  |befattning	  		|
  |adress			    	|

   #personHSAID			|enhet	|enhetsHSAID|
  #Felaktig telefonuppgift =Johan TSTNMT2321000156-107V	|TestEnhet2		|TSTNMT2321000156-107Q|
  #Har medarbetaruppdrag som är Administratör TSTNMT2321000156-1084	|TestEnhet2		|TSTNMT2321000156-107Q|
  #Felaktig befattning = Susanne TSTNMT2321000156-107W	|TestEnhet1		|TSTNMT2321000156-107P|
  #Felaktig adress = Karin TSTNMT2321000156-107T	|TestEnhet1		|TSTNMT2321000156-107P|
  @felaktigadmin
  Scenario:Skapar intyg med createDraft trots att användaren har medarbetaruppdrag som administratör
  När jag loggar in som läkare med medarbetaruppdrag som administatör
  Och att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
  Så jag går in på intygsutkastet via djupintegrationslänk
 
