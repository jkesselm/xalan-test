<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: MATCH08 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 2.1 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test of 2 predicates. -->

<xsl:template match="doc">
  <out>
    <xsl:apply-templates/>
  </out>
</xsl:template>

<xsl:template match="foo[@att1='b'][@att2='ok']">
  <xsl:value-of select="name(.)"/>
  <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="text()"/>

</xsl:stylesheet>