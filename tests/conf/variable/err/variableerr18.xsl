<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: variableerr18 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 11.2 Values of Variables and Parameters -->
  <!-- Purpose: Try to set in-template params with circular references -->
  <!-- ExpectedException: Variable defined using itself -->
  <!-- Author: David Marston -->

<xsl:template match="doc">
  <xsl:param name="circle0" select="concat('help',$circle1)"/>
  <xsl:param name="circle1" select="concat('help',$circle0)"/>
  <out>
    <xsl:value-of select="$circle0"/>
  </out>
</xsl:template>

</xsl:stylesheet>