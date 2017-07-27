package me.mrose.netcdf.sample;

import java.io.File;
import java.io.IOException;

import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class CreateArray {

    /** The lowest cadence we will ever write. */
    private static final int BASE_CADENCE = 101;

    /** Number of pixels per cadence. */
    private static final int PIXEL_COUNT = 10;

    public static void main(String[] args) throws IOException, InvalidRangeException {
        File f = new File("build/test/sample.nc");
        f.getParentFile().mkdirs();
        if (f.exists()) {
            f.delete();
        }

        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4,
            f.getAbsolutePath());

        writer.addUnlimitedDimension("cadence");
        writer.addDimension(null, "pixel", PIXEL_COUNT);

        writer.addVariable(null, "cadence", DataType.INT, "cadence");
        Variable pixel = writer.addVariable(null, "pixel", DataType.INT, "pixel");

        Variable pixels = writer.addVariable(null, "pixels", DataType.INT, "cadence pixel");
        pixels.addAttribute(new Attribute("units", "photons"));

        writer.create();

        ArrayInt.D1 pixelArr = new ArrayInt.D1(10);
        for (int i = 0; i < 10; ++i) {
            pixelArr.set(i, i + 1);
        }
        writer.write(pixel, pixelArr);

        writeCadences(writer, 101, 110);
        writeCadences(writer, 111, 120);

        writer.close();
    }

    private static void writeCadences(NetcdfFileWriter writer, int startCadence, int endCadence)
        throws IOException, InvalidRangeException {
        int cadenceCount = endCadence - startCadence + 1;
        int cadenceIndex = startCadence - BASE_CADENCE;

        Variable cadence = writer.findVariable("cadence");
        ArrayInt.D1 cadenceArr = new ArrayInt.D1(cadenceCount);
        for (int i = 0; i < cadenceCount; ++i) {
            cadenceArr.set(i, BASE_CADENCE + i);
        }
        writer.write(cadence, new int[] { cadenceIndex, 0 }, cadenceArr);

        Variable pixels = writer.findVariable("pixels");
        ArrayInt.D2 array = new ArrayInt.D2(cadenceCount, PIXEL_COUNT);
        for (int i = 0; i < cadenceCount; ++i) {
            for (int j = 0; j < 10; ++j) {
                array.set(i, j, (int) (Math.random() * 100));
            }
        }
        writer.write(pixels, new int[] { cadenceIndex, 0 }, array);
    }

}
