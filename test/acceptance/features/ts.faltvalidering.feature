# language: sv
@faltvalidering @ts 
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering TS

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

Scenariomall: Validera felaktigt <typAvFält> i <intygsTyp>
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
    Och jag klickar på signera-knappen
	Så ska valideringsfelet <feltext> visas

Exempel:
 	| intygsTyp                     			|	 typAvFält		     	| feltext       				    |
    |"Transportstyrelsens läkarintyg, diabetes"	|	insulin-datum			| "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900."			|


