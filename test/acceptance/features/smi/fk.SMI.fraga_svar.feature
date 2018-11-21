#language: sv
@luse @fråga-från-fk @vårdadmin
Egenskap: Försäkringskassan kan skicka frågor på sjukintyg LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som vårdadministratör
   När jag går in på en patient
   


Scenariomall: FK skickar fråga på "LUSE"
  När jag går in på ett "Läkarutlåtande för sjukersättning" med status "Skickad"   
	Och jag skickar intyget till Försäkringskassan
	När Försäkringskassan ställer en <ämne> fråga om intyget
	Och jag svarar på frågan
	Så ska det synas vem som svarat

@AVSTMN
    Exempel:
    |    ämne           |
    |  "AVSTMN"         | 

	Exempel:
    |    ämne           |
    |  "KONTKT"	    	|	
  	| "OVRIGT"		    |

  @vidarebefordra-mail
	Scenario: Det är möjligt att vidarebefordra frågan
  När jag går in på ett "Läkarutlåtande för sjukersättning" med status "Skickad"
  Och jag skickar intyget till Försäkringskassan
  När Försäkringskassan ställer en "OVRIGT" fråga om intyget	
  Så ska jag ha möjlighet att vidarebefordra frågan
