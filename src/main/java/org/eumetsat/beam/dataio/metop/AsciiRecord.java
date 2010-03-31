/*
 * AVISA software - $Id: AsciiRecord.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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

import org.esa.beam.framework.datamodel.MetadataElement;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.*;

/**
 * The abstract base class for all header containing infomation in ASCII format
 *
 * @author marcoz
 * @version $Revision: 1.1.1.1 $ $Date: 2007/03/22 11:12:51 $
 */
public abstract class AsciiRecord {

    private Map<String, String> map;
    private int fieldCount;

    public AsciiRecord(int fieldCount) {
        this.fieldCount = fieldCount;
        this.map = new HashMap<String, String>();
    }

    public void readRecord(ImageInputStream imageInputStream) throws IOException {
        for (int i = 0; i < fieldCount; i++) {
            final String fieldString = imageInputStream.readLine();
            final KeyValuePair field = new KeyValuePair(fieldString);

            map.put(field.key, field.value);
        }
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public int getIntValue(String key) {
        return Integer.parseInt(getValue(key));
    }

    public long getLongValue(String key) {
        return Long.parseLong(getValue(key));
    }

    abstract public MetadataElement getMetaData();

    private class KeyValuePair {
        final String key;
        final String value;

        public KeyValuePair(String field) {
            key = field.substring(0, 30).trim();
            value = field.substring(32).trim();
        }
    }

    public void printValues() {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);


        for (final String key : keys) {
            System.out.println(key + "=" + map.get(key));
        }
    }
}