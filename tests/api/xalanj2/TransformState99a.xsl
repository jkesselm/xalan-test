<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- xsl-comment-1 Filename: TransformState99a.xsl -->





<xsl:variable name="variable1" select="variable-1-value"/>
<xsl:param name="param1" select="param-1-value-default"/>


  <xsl:template match="/" name="template-1-root">
    <doc>
      <mode-header>
        <xsl:text>xsl-text-1</xsl:text>
        <xsl:value-of select="$variable1" />
        <xsl:value-of select="$param1" />
        <xsl:element name="xsl-element-1">
          <xsl:attribute name="xsl-attribute-1">xsl-attribute-1-value</xsl:attribute>xsl-element-content-newline
        </xsl:element>
      </mode-header>
      <mode-none><xsl:apply-templates select="list" /></mode-none>
      <mode-ala><xsl:call-template name="apple" /></mode-ala>
    </doc>
  </xsl:template>

  <xsl:template match="item">
    <pie><xsl:copy/></pie>
  </xsl:template>







  <xsl:template match="pies-are-good" name="apple">
<!-- Note formatting is important, this apply-templates must end on col 99; line number must match expected -->
<!-- This should be line # 40 in the file! 4567-50-234567-60-234567-70-234567-80-234567-90-23456-99 -->
    <apple>
                                                                <xsl:value-of select="count(.)" />
      <xsl:apply-templates select="list" mode="ala" />
    </apple>
  </xsl:template>



  <xsl:template match="list" mode="ala" name="list-ala-mode" >
<!-- This should be line # 50 in the file! 4567-50-234567-60-234567-70-234567-80-234567-90-23456-99 -->
    <icecream>text-literal-chars<xsl:text>xsl-text-2a</xsl:text>   <xsl:copy-of select="item[2]"/>
    </icecream>
  </xsl:template>
     




  <xsl:template match="list" name="list-no-mode" >
<!-- This should be line # 60 in the file! 4567-50-234567-60-234567-70-234567-80-234567-90-23456-99 -->
    <icemilk>text-literal-chars<xsl:text>xsl-text-3a</xsl:text>    <xsl:copy-of select="item[2]"/>
    </icemilk>
  </xsl:template>

</xsl:stylesheet>
