<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: conditionalerr16 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 9.2 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test use of non-existant variable in test attribute. -->
  <!-- ExpectedException: VariableReference given for variable out of context or without definition!  Name = level, source tree node: doc -->

<xsl:template match="doc">
  <out>
    <xsl:if test='$level=1'>1</xsl:if>
  </out>
</xsl:template>
 
</xsl:stylesheet>