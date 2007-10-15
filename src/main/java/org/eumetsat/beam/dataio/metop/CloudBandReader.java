/*
 * AVISA software - $Id: PlainBandReader.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.avhrr.AvhrrConstants;
import org.esa.beam.dataio.avhrr.AvhrrFile;
import org.esa.beam.dataio.avhrr.BandReader;
import org.esa.beam.framework.datamodel.ProductData;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Reads cloud information directly from the METOP product.
 *
 * @author marcoz
 */
class CloudBandReader implements BandReader {

    protected AvhrrFile avhrrFile;

    protected final ImageInputStream inputStream;

    public CloudBandReader(AvhrrFile avhrrFile,
                           ImageInputStream inputStream) {
        this.avhrrFile = avhrrFile;
        this.inputStream = inputStream;
    }

    public String getBandDescription() {
        return "CLAVR-x cloud mask";
    }

    public String getBandName() {
        return "cloudFlag";
    }

    public String getBandUnit() {
        return null;
    }

    public int getDataType() {
        return ProductData.TYPE_UINT16;
    }

    public float getScalingFactor() {
        return 1f;
    }

    public void readBandRasterData(int sourceOffsetX,
                                   int sourceOffsetY,
                                   int sourceWidth,
                                   int sourceHeight,
                                   int sourceStepX,
                                   int sourceStepY,
                                   final ProductData destBuffer,
                                   final ProgressMonitor pm) throws IOException {
        
        AvhrrFile.RawCoordinates rawCoord = avhrrFile.getRawCoordiantes(
                sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight);
        final short[] targetData = (short[]) destBuffer.getElems();

        pm.beginTask(MessageFormat.format("Reading AVHRR band ''{0}''...", getBandName()),
                     rawCoord.maxY - rawCoord.minY);

        int targetIdx = rawCoord.targetStart;
        for (int sourceY = rawCoord.minY; sourceY <= rawCoord.maxY; sourceY += sourceStepY) {
            if (pm.isCanceled()) {
                break;
            }

            final int dataOffset = getDataOffset(sourceOffsetX, sourceY);
            synchronized (inputStream) {
                inputStream.seek(dataOffset);
                inputStream.readFully(targetData, targetIdx, sourceWidth);
            }
            targetIdx += sourceWidth;
            pm.worked(1);
        }
        pm.done();

    }

    protected int getDataOffset(int sourceOffsetX, int sourceY) {
        return avhrrFile.getScanLineOffset(sourceY)
                + 22472
                + ((AvhrrConstants.TP_TRIM_X + sourceOffsetX) * 2);
    }

    private static String format(String pattern, String arg) {
        return new MessageFormat(pattern).format(new Object[]{arg});
    }

}