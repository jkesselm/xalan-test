<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- FileName: position88 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 3.3 -->
  <!-- Creator: David Marston, from remarks of Mukund Raghavachari (who read Mike Kay) -->
  <!-- Purpose: Test () grouping around // (descendant-or-self) axis. -->

<xsl:output method="xml" encoding="utf-8"/>

<xsl:template match="/">
  <out>
    <xsl:for-each select="(chapter//footnote)[2]">
      <greeting>
        <xsl:value-of select="."/>
      </greeting>
      <xsl:text>
</xsl:text>
    </xsl:for-each>
  </out>
</xsl:template>

</xsl:stylesheet>


