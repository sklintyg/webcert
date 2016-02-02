# language: sv
@behorighet
Egenskap: Behörigheter för en "uthoppad" vårdadministratör

@vardadmin_uthopp
Scenario: En vårdadministratör ska kunna byta vårdenhet 
   Givet att jag är inloggad som uthoppad vårdadministratör
   Och väljer att byta vårdenhet
   Så ska vårdenhet vara "WebCert-Vårdgivare1 - WebCert-Enhet1"
   När jag byter Vårdenhet
   Och väljer "WebCert-Enhet2"
   Så ska vårdenhet vara "WebCert-Vårdgivare1 - WebCert-Enhet2"




# PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR
# PRIVILEGE_ATKOMST_ANDRA_ENHETER
# PRIVILEGE_HANTERA_PERSONUPPGIFTER
# PRIVILEGE_HANTERA_MAILSVAR
# PRIVILEGE_NAVIGERING