<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                           xmlns:ppt="urn:riv:infrastructure:directory:privatepractitioner:ApprovePrivatePractitionerTermsResponder:1">

  <xsl:include href="transform/general-insuranceprocess-healthreporting-transform.xslt"/>

  <xsl:template name="response">
     <ppt:ApprovePrivatePractitionerTermsResponder>
       <ppt:result>
         <xsl:call-template name="result"/>
       </ppt:result>
     </ppt:ApprovePrivatePractitionerTermsResponder>
   </xsl:template>

</xsl:stylesheet>