# language: sv
@behorighet
Egenskap: Behörigheter för en vårdadministratör

# @vardadmin
Scenario: En vårdadministratör ska kunna kopiera Läkarintyg FK 7263 
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen tillgänglig

@vardadmin
Scenario: En vårdadministratör ska kunna kopiera Transportstyrelsens läkarintyg 
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så är kopieraknappen tillgänglig

# @vardadmin
Scenario: En vårdadministratör ska kunna kopiera Transportstyrelsens läkarintyg, diabetes intyg
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så är kopieraknappen tillgänglig

# @vardadmin
Scenario: Det ska inte gå att signera intyg som en vårdadministratör, Läkarintyg FK 7263
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

@vardadmin, @hurr
Scenario: Det ska inte gå att signera intyg som en vårdadministratör, Transportstyrelsens läkarintyg
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

# @vardadmin
Scenario: Det ska inte gå att signera intyg som en vårdadministratör, Transportstyrelsens läkarintyg, diabetes
	Givet att jag är inloggad som vårdadministratör
	Och går in på Sök/skriv intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

# PRIVILEGE_VIDAREBEFORDRA_UTKAST
# @vardadmin
Scenario: Det ska gå att Vidarebefodra ett utkast
	Givet att jag är inloggad som vårdadministratör
	Och går in på Ej signerade utkast 
	Och Vidarebeforda knappen synns
	Så avbryter jag vidarebefodran



# PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR
# PRIVILEGE_ATKOMST_ANDRA_ENHETER
# PRIVILEGE_HANTERA_PERSONUPPGIFTER
# PRIVILEGE_HANTERA_MAILSVAR
# PRIVILEGE_NAVIGERING
