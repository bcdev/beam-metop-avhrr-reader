/*
 * AVISA software - $Id: SecondaryProductHeaderRecord.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
 *
 * Copyright (C) 2005 by EUMETSAT
 *
 * The Licensee acknowledges that the AVISA software is owned by the European
 * Organisation for the Exploitation of Meteorological Satellites
 * (EUMETSAT) and the Licensee shall not transfer, assign, sub-licence,
 * reproduce or copy the AVISA software to any third party or part with
 * possession of this software or any part thereof in any way whatsoever.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * The AVISA software has been developed using the ESA BEAM software which is
 * distributed under the GNU General Public License (GPL).
 *
 */
package org.eumetsat.beam.dataio.metop;

import org.esa.beam.dataio.avhrr.HeaderUtil;
import org.esa.beam.framework.datamodel.MetadataElement;

/**
 * Reads a Second Product Header Record (SPHR)and make the contained
 * metadata available as MetadataElements
 *
 * @author marcoz
 * @version $Revision: 1.1.1.1 $ $Date: 2007/03/22 11:12:51 $
 */
class SecondaryProductHeaderRecord extends AsciiRecord {
    private static final int NUM_FIELDS = 3;

    public SecondaryProductHeaderRecord() {
        super(NUM_FIELDS);
    }

    public MetadataElement getMetaData() {
        final MetadataElement element = new MetadataElement("SECONDARY_PRODUCT_HEADER");
        element.addAttribute(HeaderUtil.createAttribute("SRC_DATA_QUAL", getIntValue("SRC_DATA_QUAL")));

        element.addAttribute(HeaderUtil.createAttribute("EARTH_VIEWS_PER_SCANLINE", getIntValue("EARTH_VIEWS_PER_SCANLINE")));
        element.addAttribute(HeaderUtil.createAttribute("NAV_SAMPLE_RATE", getIntValue("NAV_SAMPLE_RATE")));
        return element;
    }
}
