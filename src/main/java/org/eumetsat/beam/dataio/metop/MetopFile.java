/*
 * AVISA software - $Id: MetopFile.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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

import org.esa.beam.dataio.avhrr.AvhrrConstants;
import org.esa.beam.dataio.avhrr.AvhrrFile;
import org.esa.beam.dataio.avhrr.BandReader;
import org.esa.beam.dataio.avhrr.calibration.Radiance2ReflectanceFactorCalibrator;
import org.esa.beam.dataio.avhrr.calibration.Radiance2TemperatureCalibrator;
import org.esa.beam.dataio.avhrr.calibration.RadianceCalibrator;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.ProductData.UTC;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a product file containing a METOP-AVHRR/3 product.
 *
 * @author marcoz
 * @version $Revision: 1.1.1.1 $ $Date: 2007/03/22 11:12:51 $
 */
public class MetopFile extends AvhrrFile {

    private static final int EXPECTED_PRODUCT_WIDTH = 2048;

    private static final int HIGH_PRECISION_SAMPLE_RATE = 20;

    private static final int LOW_PRECISION_SAMPLE_RATE = 40;

    private static final int LOW_PRECISION_TIE_POINT_WIDTH = 51;

    private static final int HIGH_PRECISION_TIE_POINT_WIDTH = 103;

    /**
     * Specifies the difference in bytes between products
     * with a low or high precision sample rate.
     */
    private static final int TIE_POINT_DIFFERENCE = 832;

    private static final int TIE_POINT_OFFSET = 20556;

    private static final int FLAG_OFFSET = 22204;

    private static final int FRAME_INDICATOR_OFFSET = 26580;


    private final ImageInputStream inputStream;

    private GenericRecordHeader mphrHeader;

    private AsciiRecord mainProductHeaderRecord;

    private AsciiRecord secondaryProductHeaderRecord;

    private GiadrRadiance giadrRadiance;

    private int firstMdrOffset;

    private int mdrSize;

    private int numNavPoints;

    public MetopFile(ImageInputStream imageInputStream) {
        this.inputStream = imageInputStream;
    }

    public void readHeader() throws IOException {
        mphrHeader = new GenericRecordHeader();
        mphrHeader.readGenericRecordHeader(inputStream);

        if (mphrHeader.recordClass == GenericRecordHeader.RecordClass.MPHR
                && mphrHeader.instrumentGroup == GenericRecordHeader.InstrumentGroup.GENERIC
                && mphrHeader.recordSubclass == 0) {
            mainProductHeaderRecord = new MainProductHeaderRecord();
            mainProductHeaderRecord.readRecord(inputStream);
        } else {
            throw new IOException("Unsupported product: bad MPHR. RecordClass="
                    + mphrHeader.recordClass + " InstrumentGroup="
                    + mphrHeader.instrumentGroup + " RecordSubclass="
                    + mphrHeader.recordSubclass);
        }

        if (mainProductHeaderRecord.getIntValue("TOTAL_SPHR") != 1) {
            throw new IOException("Unsupported Product: more than one SPHR.");
        }
        GenericRecordHeader sphrHeader = new GenericRecordHeader();
        sphrHeader.readGenericRecordHeader(inputStream);

        if (sphrHeader.recordClass == GenericRecordHeader.RecordClass.SPHR
                && sphrHeader.instrumentGroup == GenericRecordHeader.InstrumentGroup.AVHRR3
                && sphrHeader.recordSubclass == 0) {
            secondaryProductHeaderRecord = new SecondaryProductHeaderRecord();
            secondaryProductHeaderRecord.readRecord(inputStream);
        } else {
            throw new IOException("Unsupported product: bad SPHR. RecordClass="
                    + sphrHeader.recordClass + " InstrumentGroup="
                    + sphrHeader.instrumentGroup + " RecordSubclass="
                    + sphrHeader.recordSubclass);
        }

        if (secondaryProductHeaderRecord.getIntValue("EARTH_VIEWS_PER_SCANLINE") != EXPECTED_PRODUCT_WIDTH) {
            throw new IOException("Unsupported product: bad SPHR. " +
                    "EARTH_VIEWS_PER_SCANLINE is not " + EXPECTED_PRODUCT_WIDTH + ". Actual value: " +
                    secondaryProductHeaderRecord.getIntValue("EARTH_VIEWS_PER_SCANLINE"));
        }
        final int navSampleRate = secondaryProductHeaderRecord.getIntValue("NAV_SAMPLE_RATE");
        if (navSampleRate == LOW_PRECISION_SAMPLE_RATE) {
            numNavPoints = LOW_PRECISION_TIE_POINT_WIDTH;
        } else if (navSampleRate == HIGH_PRECISION_SAMPLE_RATE) {
            numNavPoints = HIGH_PRECISION_TIE_POINT_WIDTH;
        } else {
            throw new IOException("Unsupported product: bad SPHR. " +
                    "NAV_SAMPLE_RATE is: " + navSampleRate);
        }

        final int numIPR = mainProductHeaderRecord.getIntValue("TOTAL_IPR");
        InternalPointerRecord[] iprs = new InternalPointerRecord[numIPR];

        for (int i = 0; i < numIPR; i++) {
            iprs[i] = new InternalPointerRecord();
            iprs[i].readRecord(inputStream);
            iprs[i].printIPR();
        }

        for (int i = 0; i < numIPR; i++) {
            // read GIEDR
            if (iprs[i].targetRecordClass == GenericRecordHeader.RecordClass.GIADR
                    && iprs[i].targetRecordSubclass == 1) {
                giadrRadiance = new GiadrRadiance();
                giadrRadiance.readRecord(inputStream);
            }
            if (iprs[i].targetRecordClass == GenericRecordHeader.RecordClass.GIADR
                    && iprs[i].targetRecordSubclass == 2) {
                // GiedrAnalog
                // data not read
            }
            if (iprs[i].targetRecordClass == GenericRecordHeader.RecordClass.MDR) {
                firstMdrOffset = iprs[i].targetRecordOffset;
            }
        }
        productHeight = mainProductHeaderRecord.getIntValue("TOTAL_MDR");
        productWidth = AvhrrConstants.SCENE_RASTER_WIDTH;
        checkMdrs();
        analyzeFrameIndicator();

        int toSkip = (productHeight % navSampleRate) - 1;
        if (toSkip < 0) {
            toSkip += navSampleRate;
        }
        productHeight = productHeight - toSkip;
    }

    @Override
    public String getProductName() {
        return mainProductHeaderRecord.getValue("PRODUCT_NAME");
    }

    @Override
    public UTC getStartDate() {
        return mphrHeader.recordStartTime;
    }

    @Override
    public UTC getEndDate() {
        return mphrHeader.recordEndTime;
    }

    @Override
    public List getMetaData() {
        List<MetadataElement> metaDataList = new ArrayList<MetadataElement>();

        metaDataList.add(mainProductHeaderRecord.getMetaData());
        metaDataList.add(secondaryProductHeaderRecord.getMetaData());
        metaDataList.add(giadrRadiance.getMetaData());
        return metaDataList;
    }

    @Override
    public BandReader createVisibleRadianceBandReader(int channel) {
        return new PlainBandReader(channel, this, inputStream);
    }

    @Override
    public BandReader createIrRadianceBandReader(int channel) {
        return new PlainBandReader(channel, this, inputStream);
    }

    @Override
    public BandReader createReflectanceFactorBandReader(int channel) {
        RadianceCalibrator radianceCalibrator = new Radiance2ReflectanceFactorCalibrator(
                giadrRadiance.getEquivalentWidth(channel), giadrRadiance
                .getSolarIrradiance(channel), 1);
        //TODO this 1 should be the earth-sun-distance-ratio, but this ratio is always 0.
        return new CalibratedBandReader(channel, this, inputStream,
                                        radianceCalibrator);
    }

    @Override
    public BandReader createIrTemperatureBandReader(int channel) {
        RadianceCalibrator radianceCalibrator = new Radiance2TemperatureCalibrator(
                giadrRadiance.getConstant1(channel), giadrRadiance
                .getConstant2(channel), giadrRadiance
                .getCentralWavenumber(channel));
        return new CalibratedBandReader(channel, this, inputStream,
                                        radianceCalibrator);
    }

    public int getNumNavPoints() {
        return numNavPoints;
    }

    public int getNavSampleRate() {
        return secondaryProductHeaderRecord.getIntValue("NAV_SAMPLE_RATE");
    }

    @Override
    public String[] getTiePointNames() {
        return new String[]{AvhrrConstants.SZA_DS_NAME,
                AvhrrConstants.VZA_DS_NAME, AvhrrConstants.SAA_DS_NAME,
                AvhrrConstants.VAA_DS_NAME, AvhrrConstants.LAT_DS_NAME,
                AvhrrConstants.LON_DS_NAME};
    }

    @Override
    public float[][] getTiePointData() throws IOException {
        final int navSampleRate = getNavSampleRate();
        final int gridHeight = getProductHeight()
                / navSampleRate + 1;
        final int numNavPoints = getNumNavPoints();
        final int numTiePoints = numNavPoints * gridHeight;

        float[][] tiePointData = new float[6][numTiePoints];
        final int numRawAngles = numNavPoints * 4;
        final int numRawLatLon = numNavPoints * 2;

        short[] rawAngles = new short[numRawAngles];
        int[] rawLatLon = new int[numRawLatLon];

        int targetIndex = 0;
        int targetIncr = 1;

        for (int scanLine = 0; scanLine < getProductHeight(); scanLine += navSampleRate) {
            final int scanLineOffset = getScanLineOffset(scanLine);
            synchronized (inputStream) {
                inputStream.seek(scanLineOffset + TIE_POINT_OFFSET);
                inputStream.readFully(rawAngles, 0, numRawAngles);
                inputStream.readFully(rawLatLon, 0, numRawLatLon);
            }
            for (int scanPoint = 0; scanPoint < numNavPoints; scanPoint++) {
                tiePointData[0][targetIndex] = rawAngles[scanPoint * 4] * 1E-2f;
                tiePointData[1][targetIndex] = rawAngles[scanPoint * 4 + 1] * 1E-2f;
                tiePointData[2][targetIndex] = rawAngles[scanPoint * 4 + 2] * 1E-2f;
                tiePointData[3][targetIndex] = rawAngles[scanPoint * 4 + 3] * 1E-2f;

                tiePointData[4][targetIndex] = rawLatLon[scanPoint * 2] * 1E-4f;
                tiePointData[5][targetIndex] = rawLatLon[scanPoint * 2 + 1] * 1E-4f;
                targetIndex += targetIncr;
            }
        }
        return tiePointData;
    }

    @Override
    public int getScanLineOffset(int rawY) {
        return firstMdrOffset + (rawY * mdrSize); // 26660 MDR-1B size
    }

    @Override
    public int getFlagOffset(int rawY) {
        int flagOffset = getScanLineOffset(rawY) + FLAG_OFFSET;
        if (numNavPoints == LOW_PRECISION_TIE_POINT_WIDTH) {
            flagOffset = flagOffset - TIE_POINT_DIFFERENCE;
        }
        return flagOffset;
    }

    public int readFrameIndicator(int rawY) throws IOException {
        int flagOffset = getScanLineOffset(rawY) + FRAME_INDICATOR_OFFSET + 1;
        if (numNavPoints == LOW_PRECISION_TIE_POINT_WIDTH) {
            flagOffset = flagOffset - TIE_POINT_DIFFERENCE;
        }
        byte flag;
        synchronized (inputStream) {
            inputStream.seek(flagOffset);
            flag = inputStream.readByte();
        }
        return flag;
    }

    public static boolean canOpenFile(File file) throws IOException {
        ImageInputStream inputStream = new FileImageInputStream(file);
        try {
            GenericRecordHeader mphrHeader = new GenericRecordHeader();
            mphrHeader.readGenericRecordHeader(inputStream);

            // check for MPHR
            if (mphrHeader.recordClass == GenericRecordHeader.RecordClass.MPHR
                    && mphrHeader.instrumentGroup == GenericRecordHeader.InstrumentGroup.GENERIC
                    && mphrHeader.recordSubclass == 0) {

                inputStream.seek(mphrHeader.recordSize);
                GenericRecordHeader sphrHeader = new GenericRecordHeader();
                sphrHeader.readGenericRecordHeader(inputStream);

                // check for SPHR and AVHRR/3
                if (sphrHeader.recordClass == GenericRecordHeader.RecordClass.SPHR
                        && sphrHeader.instrumentGroup == GenericRecordHeader.InstrumentGroup.AVHRR3
                        && sphrHeader.recordSubclass == 0) {
                    return true;
                }
            }
        } finally {
            inputStream.close();
        }
        return false;
    }

    private void analyzeFrameIndicator() throws IOException {
        final int first = readFrameIndicator(0);
        final int last = readFrameIndicator(getProductHeight() - 1);

        final int firstChannel3ab = first & 1;
        final int lastChannel3ab = last & 1;
        if (firstChannel3ab == 1 && lastChannel3ab == 1) {
            channel3ab = AvhrrConstants.CH_3A;
        } else if (firstChannel3ab == 0 && lastChannel3ab == 0) {
            channel3ab = AvhrrConstants.CH_3B;
        } else {
            channel3ab = -1;
        }
    }

    private void checkMdrs() throws IOException {
        GenericRecordHeader firstMdr = new GenericRecordHeader();
        synchronized (inputStream) {
            inputStream.seek(firstMdrOffset);
            firstMdr.readGenericRecordHeader(inputStream);
        }
        mdrSize = (int) firstMdr.recordSize;

        final long fileSize = inputStream.length();
        final long expectedFileSize = firstMdrOffset + (productHeight * mdrSize);
        if (fileSize != expectedFileSize) {
            throw new IOException("Product has wrong file size. Expected filesize: " + expectedFileSize
                    + " Actual filesize: " + fileSize);
        }
    }
}
