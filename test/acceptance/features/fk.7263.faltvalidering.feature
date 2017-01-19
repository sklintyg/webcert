# language: sv
@faltvalidering @fk7263 
Egenskap: Fältvalidering fk7263

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient


Scenariomall: Validera felaktigt <typAvFält> i <intygsTyp>
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
   # Och jag klickar på signera-knappen
	Så ska valideringsfelet <feltext> visas
	Och ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
 	Och ska jag se en lista med endast det saknade <fältet>
 	Och jag ändrar till giltig text i <typAvFält>
 	Så ska varken <feltext> eller info om det saknade <fältet> finnas kvar
 	Så ska utkastets statusheader meddela <meddelande>

Exempel:
 	| intygsTyp                     |	 typAvFält		     	| feltext       				    | fältet	| meddelande |
    |"Läkarintyg FK 7263"	|	"UndersökningsDatum"	| "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"			| "Intyget baseras på" |  "Utkastet är sparat, men obligatoriska uppgifter saknas." |

