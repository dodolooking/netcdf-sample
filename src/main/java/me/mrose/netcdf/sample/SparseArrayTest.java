package me.mrose.netcdf.sample;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingDefault;

public class SparseArrayTest {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        long start = System.currentTimeMillis();

        File f = new File("build/test/sparse-array.nc");
        f.getParentFile().mkdirs();
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();

        Nc4Chunking chunker = Nc4ChunkingDefault.factory(Nc4Chunking.Strategy.standard, 5, true);

        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4,
            f.getAbsolutePath(), chunker);

        writer.addUnlimitedDimension("key");

        Variable data = writer.addVariable(null, "data", DataType.FLOAT, "key");
        data.addAttribute(new Attribute(CDM.FILL_VALUE, Float.NaN));

        Variable key = writer.addVariable(null, "key", DataType.DOUBLE, "key");
        key.addAttribute(new Attribute(CDM.FILL_VALUE, Double.NaN));

        writer.create();

        Map<Double, Float> map = new TreeMap<>();
        for (double k : new double[] { 1.0, 2.0, 3.0, 4.0 }) {
            map.put(k, (float) (k * k));
        }

        writeValues(writer, key, data, map);
        writer.close();

        long end = System.currentTimeMillis();
        System.out.println(String.format("Total time to write: %.1f seconds",
            (end - start) / 1000.0));

        writer = NetcdfFileWriter.openExisting(f.getAbsolutePath());
        showValues(writer);
        writer.close();

        // Shorten the dimension of the arrays.
        writer = NetcdfFileWriter.openExisting(f.getAbsolutePath());
        key = writer.findVariable("key");
        data = writer.findVariable("data");
        map.remove(2.0);
        data.resetDimensions();
        key.resetDimensions();

        // Write the new, shorter map values.
        writeValues(writer, key, data, map);
        writer.close();

        writer = NetcdfFileWriter.openExisting(f.getAbsolutePath());
        showValues(writer);
        writer.close();
    }

    private static void writeValues(NetcdfFileWriter writer, Variable key, Variable data,
        Map<Double, Float> map) throws IOException, InvalidRangeException {

        int length = Math.max(map.size(), data.getDimension(0).getLength());
        ArrayDouble.D1 keyArray = new ArrayDouble.D1(length);
        ArrayFloat.D1 valueArray = new ArrayFloat.D1(length);

        int i = 0;
        for (Map.Entry<Double, Float> entry : map.entrySet()) {
            keyArray.set(i, entry.getKey());
            valueArray.set(i, entry.getValue());
            ++i;
        }

        while (i < length) {
            keyArray.set(i, Double.NaN);
            valueArray.set(i, Float.NaN);
            ++i;
        }

        writer.write(data, new int[] { 0 }, valueArray);
        writer.write(key, new int[] { 0 }, keyArray);
    }

    private static void showValues(NetcdfFileWriter writer) throws IOException,
        InvalidRangeException {

        Variable data = writer.findVariable("data");
        Variable key = writer.findVariable("key");

        int length = data.getDimension(0).getLength();
        ArrayFloat.D1 valueArray = (ArrayFloat.D1) data.read(new int[] { 0 }, new int[] { length });
        ArrayDouble.D1 keyArray = (ArrayDouble.D1) key.read(new int[] { 0 }, new int[] { length });

        for (int i = 0; i < length; ++i) {
            if (!Double.isNaN(keyArray.get(i))) {
                System.out.println(String.format("%g --> %g", keyArray.get(i), valueArray.get(i)));
            }
        }

    }

}
