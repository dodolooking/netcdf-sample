package me.mrose.netcdf.sample;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;

import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingDefault;

public class VariableDimensionTest {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        File f = new File("build/test/variable-array.nc");
        f.getParentFile().mkdirs();
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();

        Nc4Chunking chunker = Nc4ChunkingDefault.factory(Nc4Chunking.Strategy.standard, 5, true);

        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4,
            f.getAbsolutePath(), chunker);

        Dimension indexDim = writer.addDimension(null, "index", 10, false, false, false);
        Dimension byteDim = writer.addDimension(null, "byte", -1, false, false, true);
        Variable data = writer.addVariable(null, "data", DataType.BYTE,
            ImmutableList.of(indexDim, byteDim));

        writer.create();
        writer.close();

        data = writer.findVariable("data");

        ArrayByte.D1 values = new ArrayByte.D1(5);
        for (int i = 1; i < 5; ++i) {
            values.set(i - 1, (byte) i);
        }
        writer.write(data, new int[] { 0 }, values);

        writer.close();
    }

}
