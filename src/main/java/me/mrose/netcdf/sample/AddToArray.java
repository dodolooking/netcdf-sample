package me.mrose.netcdf.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dataset.NetcdfDataset;

public class AddToArray {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        long start = System.currentTimeMillis();

        File f = new File("build/test/sample.nc");
        if (!f.exists()) {
            throw new FileNotFoundException(f.getPath() + " must exist");
        }

        NetcdfDataset dataset = NetcdfDataset.openDataset(f.getAbsolutePath());

        dataset.addVariable(null, "another", DataType.INT, "cadence");
        dataset.finish();

        dataset.close();

        long end = System.currentTimeMillis();
        System.out.println(String.format("Total time: %.1f seconds", (end - start) / 1000.0));
    }

}
