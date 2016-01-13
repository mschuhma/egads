package com.yahoo.egads.utilities;

import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by mschuhmacher on 1/13/16.
 */
public class StreamUtils {

    private static BufferedWriter outputWriter = null;
    public static final String PERMANENT_OUTPUT_FILE = "egads_output.gz";

    public static BufferedReader getBufferedReader(String file) throws IOException{

        if (file.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            return new BufferedReader(new InputStreamReader(gzip));
        }

        else if (file.endsWith(".xz")) {
            XZInputStream xz = new XZInputStream(new FileInputStream(file));
            return new BufferedReader(new InputStreamReader(xz));
        }

        else {
            return new BufferedReader(new FileReader(file));
        }

    }

    public static BufferedWriter getPermanentOutputWriter() throws IOException{

        if (outputWriter == null)
            outputWriter = getBufferedWriter(PERMANENT_OUTPUT_FILE);
        return outputWriter;
    }

    public static BufferedWriter getBufferedWriter(String file) throws IOException {

        if (file.endsWith(".gz")) {
            GZIPOutputStream stream = new GZIPOutputStream(new FileOutputStream(file));
            return new BufferedWriter(new OutputStreamWriter(stream));
        }

        else if (file.endsWith(".xz")) {
            XZOutputStream stream = new XZOutputStream(new FileOutputStream(file), new LZMA2Options());
            return new BufferedWriter(new OutputStreamWriter(stream));
        }

        else {
            return new BufferedWriter(new FileWriter(file));
        }

    }

}
