/*
 * AVISA software - $Id: MetopReader.java,v 1.1.1.1 2007/03/22 11:12:51 ralf Exp $
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
import org.esa.beam.dataio.avhrr.AvhrrReader;
import org.esa.beam.framework.dataio.IllegalFileFormatException;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.TiePointGeoCoding;
import org.esa.beam.framework.datamodel.TiePointGrid;
import org.esa.beam.framework.dataop.maptransf.Datum;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.IOException;


/**
 * A reader for METOP-AVHRR/3 Level-1b data products.
 */
public class MetopReader extends AvhrrReader implements AvhrrConstants {

    public MetopReader(ProductReaderPlugIn metopReaderPlugIn) {
        super(metopReaderPlugIn);
    }

    /**
     * Provides an implementation of the <code>readProductNodes</code>
     * interface method. Clients implementing this method can be sure that the
     * input object and eventually the subset information has already been set.
     * <p/>
     * <p/>
     * This method is called as a last step in the
     * <code>readProductNodes(input, subsetInfo)</code> method.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    protected Product readProductNodesImpl() throws IOException,
            IllegalFileFormatException {
        final File dataFile = MetopReaderPlugIn.getInputFile(getInput());

        try {
            imageInputStream = new FileImageInputStream(dataFile);
            avhrrFile = new MetopFile(imageInputStream);
            avhrrFile.readHeader();
            createProduct();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                close();
            } catch (IOException ignored) {
                // ignore
            }
            throw e;
        }
        product.setFileLocation(dataFile);

        return product;
    }

    @Override
    protected void addTiePointGrids() throws IOException {
        final MetopFile metopFile = (MetopFile) avhrrFile;
        final int tiePointSampleRate = metopFile.getNavSampleRate();
        final int tiePointGridHeight = metopFile.getProductHeight() / tiePointSampleRate + 1;
        final int tiePointGridWidth = metopFile.getNumNavPoints();

        String[] tiePointNames = avhrrFile.getTiePointNames();
        float[][] tiePointData = avhrrFile.getTiePointData();

        final int numGrids = tiePointNames.length;
        TiePointGrid grid[] = new TiePointGrid[numGrids];

        for (int i = 0; i < grid.length; i++) {
            grid[i] = createTiePointGrid(tiePointNames[i], tiePointGridWidth,
                                         tiePointGridHeight, TP_OFFSET_X,
                                         TP_OFFSET_Y, tiePointSampleRate, tiePointSampleRate,
                                         tiePointData[i]);
            grid[i].setUnit(UNIT_DEG);
            product.addTiePointGrid(grid[i]);
        }
        addDeltaAzimuth(tiePointGridWidth, tiePointGridHeight, tiePointSampleRate);

        GeoCoding geoCoding = new TiePointGeoCoding(grid[numGrids - 2],
                                                    grid[numGrids - 1], Datum.WGS_72);
        product.setGeoCoding(geoCoding);
    }

    private void addDeltaAzimuth(int tiePointGridWidth, int tiePointGridHeight, int tiePointSampleRate) {
        float[] sunAzimuthTiePointData = product.getTiePointGrid(SAA_DS_NAME).getTiePoints();
        float[] viewAzimuthTiePointData = product.getTiePointGrid(VAA_DS_NAME).getTiePoints();
        final int numTiePoints = viewAzimuthTiePointData.length;
        float[] deltaAzimuthData = new float[numTiePoints];

        for (int i = 0; i < numTiePoints; i++) {
            deltaAzimuthData[i] = (float) computeAda(viewAzimuthTiePointData[i], sunAzimuthTiePointData[i]);
        }

        TiePointGrid grid = createTiePointGrid(DAA_DS_NAME, tiePointGridWidth,
                                               tiePointGridHeight, TP_OFFSET_X, TP_OFFSET_Y, tiePointSampleRate,
                                               tiePointSampleRate, deltaAzimuthData);
        grid.setUnit(UNIT_DEG);
        product.addTiePointGrid(grid);
    }

    /**
     * Computes the azimuth difference from the given
     *
     * @param vaa viewing azimuth angle [degree]
     * @param saa sun azimuth angle [degree]
     * @return the azimuth difference [degree]
     */
    private static double computeAda(double vaa, double saa) {
        double ada = saa - vaa;
        if (ada <= -180.0) {
            ada += 360.0;
        } else if (ada > +180.0) {
            ada -= 360.0;
        }
        return ada;
    }

    public static boolean canOpenFile(File file) {
        try {
            return MetopFile.canOpenFile(file);
        } catch (IOException e) {
            return false;
        }
    }

}