<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                           xmlns:ra="urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1">

  <xsl:include href="transform/general-transform.xslt"/>

  <xsl:template name="response">
     <ra:ReceiveMedicalCertificateAnswerResponse>
       <ra:result>
         <xsl:call-template name="result"/>
       </ra:result>
     </ra:ReceiveMedicalCertificateAnswerResponse>
   </xsl:template>

</xsl:stylesheet>