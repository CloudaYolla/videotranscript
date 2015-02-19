<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

    <xsl:template match="/transcript">
<!--         <transcript> -->
	INSERT INTO transcript (start, dur, text) VALUES
      <xsl:for-each select="text">
      ('<xsl:value-of select="@start" />', <xsl:value-of select="@dur" />, '<xsl:value-of select="."/>'), 
<!--       INSERT INTO transcript (start, dur, text) VALUES ('<xsl:value-of select="@start" />', <xsl:value-of select="@dur" />, '<xsl:value-of select="."/>');  -->
</xsl:for-each>
      ('-1.0', -1, '')
	;
<!--         </transcript> -->
    </xsl:template>
</xsl:stylesheet>