package me.mrose.netcdf.sample;

import java.io.File;
import java.io.IOException;

import ucar.ma2.ArrayFloat;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class ReadArray {

    /** The lowest cadence we will ever write. */
    private static final int BASE_CADENCE = 101;

    private static final int CADENCE_CHUNK = 600;

    public static void main(String[] args) throws IOException, InvalidRangeException {
        long start = System.currentTimeMillis();

        File f = new File("build/test/sample.nc");
        NetcdfFileWriter writer = NetcdfFileWriter.openExisting(f.getAbsolutePath());

        Variable pixels = writer.findVariable("pixels");
        Dimension cadenceDim = pixels.getDimension(0);
        Dimension pixelDim = pixels.getDimension(1);

        int cadenceCount = 0;
        while (cadenceCount < cadenceDim.getLength()) {
            int chunkSize = Math.min(cadenceDim.getLength() - cadenceCount, CADENCE_CHUNK);

            readCadences(writer, BASE_CADENCE + cadenceCount, chunkSize, pixelDim.getLength());
            cadenceCount += chunkSize;
        }

        writer.close();

        long end = System.currentTimeMillis();
        System.out.println(String.format("Total time: %.1f seconds", (end - start) / 1000.0));
    }

    private static void readCadences(NetcdfFileWriter writer, int startCadence, int cadenceCount,
        int pixelCount) throws IOException, InvalidRangeException {

        int cadenceIndex = startCadence - BASE_CADENCE;
        Variable pixels = writer.findVariable("pixels");

        ArrayFloat.D2 array = (ArrayFloat.D2) pixels.read(new int[] { cadenceIndex, 0 }, new int[] {
            cadenceCount, pixelCount });

        float[][] values = new float[array.getShape()[0]][array.getShape()[1]];
        for (int i = 0; i < values.length; ++i) {
            for (int j = 0; j < values[i].length; ++j) {
                values[i][j] = array.get(i, j);
            }
        }
    }

}
