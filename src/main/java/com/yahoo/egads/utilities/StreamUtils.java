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

        BufferedReader br = null;

        if (file.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(gzip));
        }

        if (file.endsWith(".xz")) {
            XZInputStream xz = new XZInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(xz));
        }

        else {
            br = new BufferedReader(new FileReader(file));
        }

        return br;
    }

    public static BufferedWriter getPermanentOutputWriter() throws IOException{

        if (outputWriter == null)
            outputWriter = getBufferedWriter(PERMANENT_OUTPUT_FILE);
        return outputWriter;
    }

    public static BufferedWriter getBufferedWriter(String file) throws IOException {

        BufferedWriter bw = null;


        if (file.endsWith(".gz")) {
            GZIPOutputStream stream = new GZIPOutputStream(new FileOutputStream(file));
            bw = new BufferedWriter(new OutputStreamWriter(stream));
        }

        if (file.endsWith(".xz")) {
            XZOutputStream stream = new XZOutputStream(new FileOutputStream(file), new LZMA2Options());
            bw = new BufferedWriter(new OutputStreamWriter(stream));
        }

        else {
            bw = new BufferedWriter(new FileWriter(file));
        }

        return bw;

    }

}
