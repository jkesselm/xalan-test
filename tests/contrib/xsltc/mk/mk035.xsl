<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Test FileName: mk035.xsl -->
  <!-- Source Attribution: 
       This test was written by Michael Kay and is taken from 
       'XSLT Programmer's Reference' published by Wrox Press Limited in 2000;
       ISBN 1-861003-12-9; copyright Wrox Press Limited 2000; all rights reserved. 
       Now updated in the second edition (ISBN 1861005067), http://www.wrox.com.
       No part of this book may be reproduced, stored in a retrieval system or 
       transmitted in any form or by any means - electronic, electrostatic, mechanical, 
       photocopying, recording or otherwise - without the prior written permission of 
       the publisher, except in the case of brief quotations embodied in critical articles or reviews.
  -->
  <!-- Example: cities.xml, count-countries.xsl -->
  <!-- Chapter/Page: 7-436 -->
  <!-- Purpose: Counting distinct values (count function) -->

<xsl:template match="/">
<count>
<xsl:value-of 
      select="count(//city[not(@country=preceding::city/@country)])"/>
</count>
</xsl:template>
</xsl:stylesheet>