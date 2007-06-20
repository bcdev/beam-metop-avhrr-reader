/*
 * AVISA software - $Id: InternalPointerRecord.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * Reads an Internal Pointer record (IPR) and holds it's data
 * in accessible fields.
 *
 * @author marcoz
 * @version $Revision: 1.1.1.1 $ $Date: 2007/03/22 11:12:51 $
 */
class InternalPointerRecord {

    GenericRecordHeader header;

    int targetRecordClass;
    int targetInstrumentGroup;
    int targetRecordSubclass;
    int targetRecordOffset;

    public void readRecord(ImageInputStream imageInputStream) throws IOException {
        header = new GenericRecordHeader();
        header.readGenericRecordHeader(imageInputStream);
        if (header.recordClass != GenericRecordHeader.RecordClass.IPR ||
                header.instrumentGroup != GenericRecordHeader.InstrumentGroup.GENERIC) {
            throw new IllegalArgumentException("bad product"); // TODO
        }
        header.printGRH();
        targetRecordClass = imageInputStream.readByte();
        targetInstrumentGroup = imageInputStream.readByte();
        targetRecordSubclass = imageInputStream.readByte();
        targetRecordOffset = (int) imageInputStream.readUnsignedInt();
    }

    void printIPR() {
        System.out.println("targetRecordClass " + targetRecordClass);
        System.out.println("targetInstrumentGroup " + targetInstrumentGroup);
        System.out.println("targetRecordSubclass " + targetRecordSubclass);
        System.out.println("targetRecordOffset " + targetRecordOffset);
    }
}
