package com.yahoo.egads.utilities;

import org.tukaani.xz.XZInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Created by mschuhmacher on 1/13/16.
 */
public class StreamUtils {

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

}
