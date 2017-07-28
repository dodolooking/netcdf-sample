package me.mrose.netcdf.sample;

import java.io.File;
import java.io.IOException;

import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingDefault;

public class CreateArray {

    /** The lowest cadence we will ever write. */
    private static final int BASE_CADENCE = 101;

    private static final int CADENCE_CHUNK = 600;

    private static final int CADENCE_COUNT = 30 * 24 * 30;

    /** Number of pixels per cadence. */
    private static final int PIXEL_COUNT = 121 * 1000;

    public static void main(String[] args) throws IOException, InvalidRangeException {
        long start = System.currentTimeMillis();

        File f = new File("build/test/sample.nc");
        f.getParentFile().mkdirs();
        if (f.exists()) {
            f.delete();
        }

        Nc4Chunking chunker = Nc4ChunkingDefault.factory(Nc4Chunking.Strategy.standard, 5, true);

        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4,
            f.getAbsolutePath(), chunker);

        writer.addUnlimitedDimension("cadence");
        writer.addUnlimitedDimension("pixel");

        Variable pixels = writer.addVariable(null, "pixels", DataType.FLOAT, "cadence pixel");
        pixels.addAttribute(new Attribute("units", "photons"));
        pixels.addAttribute(new Attribute(CDM.FILL_VALUE, Float.NaN));

        writer.create();

        for (int i = 0; i < CADENCE_COUNT; i += CADENCE_CHUNK) {
            int chunkSize = Math.min(CADENCE_CHUNK, CADENCE_COUNT - i);

            writeCadences(writer, BASE_CADENCE + i, BASE_CADENCE + i + chunkSize - 1, PIXEL_COUNT);
        }

        writer.close();

        long end = System.currentTimeMillis();
        System.out.println(String.format("Total time: %.1f seconds", (end - start) / 1000.0));
    }

    private static void writeCadences(NetcdfFileWriter writer, int startCadence, int endCadence,
        int pixelCount) throws IOException, InvalidRangeException {

        int cadenceCount = endCadence - startCadence + 1;
        int cadenceIndex = startCadence - BASE_CADENCE;

        Variable pixels = writer.findVariable("pixels");
        ArrayFloat.D2 array = new ArrayFloat.D2(cadenceCount, pixelCount);
        for (int i = 0; i < cadenceCount; ++i) {
            for (int j = 0; j < pixelCount; ++j) {
                array.set(i, j, (float) Math.random());
            }
        }
        writer.write(pixels, new int[] { cadenceIndex, 0 }, array);
    }

}
