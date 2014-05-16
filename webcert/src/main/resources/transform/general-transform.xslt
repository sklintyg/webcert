<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:hr="urn:riv:insuranceprocess:healthreporting:2">

  <!-- Copy all XML nodes, if no more specific template matches. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Transform <soap:Fault> element into a proper WS response. -->
  <xsl:template match="soap:Fault">
    <!--
      Here we call the 'abstract' template named response. Has to be provided by
      all XSLTs which include this one.
      -->
    <xsl:call-template name="response"/>
  </xsl:template>

  <!-- Transform <faultcode> and <faultstring> elements to <resultCode>, <errorId> and <errorText> -->
  <xsl:template name="result">
    <hr:resultCode>ERROR</hr:resultCode>

    <xsl:choose>
      <xsl:when test="contains(faultstring/text(), 'Unmarshalling Error')">
        <!-- Schema validation errors are transformed to VALIDATION_ERROR -->
        <hr:errorId>VALIDATION_ERROR</hr:errorId>
      </xsl:when>
      <xsl:when test="contains(faultcode/text(), 'soap:Client')">
        <!-- 'soap:Client' is transformed to VALIDATION_ERROR -->
        <hr:errorId>VALIDATION_ERROR</hr:errorId>
      </xsl:when>
      <xsl:otherwise>
        <!-- 'soap:Server' is transformed to APPLICATION_ERROR -->
        <hr:errorId>APPLICATION_ERROR</hr:errorId>
      </xsl:otherwise>
    </xsl:choose>

    <hr:errorText>
      <xsl:value-of select="faultstring/text()"/>
    </hr:errorText>
  </xsl:template>


</xsl:stylesheet>