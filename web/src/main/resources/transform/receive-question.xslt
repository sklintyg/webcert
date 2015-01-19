<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rq="urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1">

  <xsl:include href="transform/general-insuranceprocess-healthreporting-transform.xslt"/>

  <xsl:template name="response">
     <rq:ReceiveMedicalCertificateQuestionResponse>
       <rq:result>
         <xsl:call-template name="result"/>
       </rq:result>
     </rq:ReceiveMedicalCertificateQuestionResponse>
   </xsl:template>

</xsl:stylesheet>