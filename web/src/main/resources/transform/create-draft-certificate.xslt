<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cdc="urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:1">

  <xsl:include href="transform/general-transform.xslt"/>

  <xsl:template name="response">
     <cdc:CreateDraftCertificateResponse>
       <cdc:result>
         <xsl:call-template name="result"/>
       </cdc:result>
     </cdc:CreateDraftCertificateResponse>
   </xsl:template>

</xsl:stylesheet>