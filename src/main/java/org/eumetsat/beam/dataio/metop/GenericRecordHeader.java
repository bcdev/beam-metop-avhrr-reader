/*
 * AVISA software - $Id: GenericRecordHeader.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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

import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.ProductData.UTC;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * Can read a Generic Record Header (GRH) and holds it's information
 * in public accessible fields.
 *
 * @author marcoz
 * @version $Revision: 1.1.1.1 $ $Date: 2007/03/22 11:12:51 $
 */
class GenericRecordHeader {

    public int recordClass;

    public int instrumentGroup;

    public int recordSubclass;

    public int recordSubclassVersion;

    public long recordSize;

    public ProductData.UTC recordStartTime;

    public ProductData.UTC recordEndTime;

    public void readGenericRecordHeader(ImageInputStream imageInputStream) throws IOException {
        recordClass = imageInputStream.readByte();
        instrumentGroup = imageInputStream.readByte();
        recordSubclass = imageInputStream.readByte();
        recordSubclassVersion = imageInputStream.readByte();
        recordSize = imageInputStream.readUnsignedInt();
        int day = imageInputStream.readUnsignedShort();
        long millis = imageInputStream.readUnsignedInt();
        recordStartTime = new UTC(day, (int) millis / 1000, (int) millis % 1000);
        day = imageInputStream.readUnsignedShort();
        millis = imageInputStream.readUnsignedInt();
        recordEndTime = new UTC(day, (int) millis / 1000, (int) millis % 1000);
    }

    public void printGRH() {
        System.out.println("recordClass: " + recordClass);
        System.out.println("instrumentGroup: " + instrumentGroup);
        System.out.println("recordSubclass: " + recordSubclass);
        System.out.println("recordSubclassVersion: " + recordSubclassVersion);
        System.out.println("recordSize: " + recordSize);
        System.out.println("recordStartTime: " + recordStartTime);
        System.out.println("recordEndTime: " + recordEndTime);
    }

    public class RecordClass {
        public static final int MPHR = 1;
        public static final int SPHR = 2;
        public static final int IPR = 3;
        public static final int GEADR = 4;
        public static final int GIADR = 5;
        public static final int VEADR = 6;
        public static final int VIADR = 7;
        public static final int MDR = 8;
    }

    public class InstrumentGroup {
        public static final int GENERIC = 0;
        public static final int ATOVS = 3;
        public static final int AVHRR3 = 4;
    }

}
