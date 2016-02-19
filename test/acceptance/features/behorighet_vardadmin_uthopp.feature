# language: sv
@behorighet @notReady
# PRIVILEGE_NAVIGERING
Egenskap: Behörigheter för en uthoppad vårdadministratör

#@vardadmin_uthopp1
Scenario: Kan byta vårdenhet 
   Givet att jag är inloggad som vårdadministratör
   #Givet att jag är inloggad som uthoppad vårdadministratör
   Och vårdenhet ska vara "WebCert-Vårdgivare2 - WebCert-Enhet2"
   När jag väljer att byta vårdenhet
   Och väljer "WebCert-Enhet2"
   Så vårdenhet ska vara "WebCert-Vårdgivare2 - WebCert-Enhet2"

# PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR

# PRIVILEGE_VIDAREBEFORDRA_UTKAST
# PRIVILEGE_ATKOMST_ANDRA_ENHETER
#@vardadmin_uthopp
Scenario: Kan vidarebefodra ett utkast
	Givet att jag är inloggad som vårdadministratör
	#Givet att jag är inloggad som uthoppad vårdadministratör
	Och går in på Ej signerade utkast 
	Så synns Vidarebefodra knappen

# Skriva utkast, Läsa intyg/utkast
#@vardadmin_uthopp
Scenario: Kan ej signera intyg.
	Givet att jag är inloggad som vårdadministratör
	#Givet att jag är inloggad som uthoppad vårdadministratör
	När jag väljer flik "Sök/skriv intyg"
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så synns inte signera knappen

# PRIVILEGE_HANTERA_PERSONUPPGIFTER
# - Visar "Hämta personuppgifter"-knapp på utkast
#@vardadmin_uthopp
Scenario: Kan visa "Hämta personuppgifter"-knapp på utkast
	Givet att jag är inloggad som vårdadministratör
		När jag väljer flik "Sök/skriv intyg"
		När jag väljer patienten "19971019-2387"
		Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
		Så synns Hämta personuppgifter knappen
		

# - Visar information om sekretessmarkerade personuppgifter
#@vardadmin_uthopp
Scenario: Kan visa information om sekretessmarkerade personuppgifter
	Givet att jag är inloggad som vårdadministratör
		När jag väljer flik "Sök/skriv intyg"
		När jag väljer patienten "19080814-9819"
		Och jag kan inte gå in på att skapa ett "Läkarintyg FK 7263" intyg
		Så meddelas jag om spärren

# PRIVILEGE_HANTERA_MAILSVAR
#@vardadmin_uthopp
#Scenario: Det ska gå att Vidarebefodra ett utkast
	#Givet att jag är inloggad som uthoppad vårdadministratör
	#Givet att jag är inloggad som vårdadministratör
	#När jag väljer flik "Sök/skriv intyg"
	#När jag väljer patienten "19121212-1212"
	#Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
	#Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
	#Så jag markerar frågan från Försäkringskassan som hanterad

