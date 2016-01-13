/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.egads.utilities;

// Class that implements EGADS file input processing.

import com.yahoo.egads.control.ProcessableObject;
import com.yahoo.egads.control.ProcessableObjectFactory;
import com.yahoo.egads.data.TimeSeries;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;


/*
Processor to read files containing multiple, sparse time series. Possible data formats: </br>
File has to be sorted by time.
time_series_name,datetime,value</br>
time_series_name datetime value</br>
time_series_name \t datetime \t value</br>
 */

public class BatchFileInputProcessor implements InputProcessor {

    private String file = null;
    private BufferedReader br = null;
    private boolean fillMissing = false;

    private final static String COLUMN_DELIMITER = ",";

    public BatchFileInputProcessor(String file) {
        this.file = file;
    }

    public void processInput(Properties p) throws Exception {

        // Setup config
        if (p.getProperty("FILL_MISSING") != null && p.getProperty("FILL_MISSING").equals("1")) {
            fillMissing = true;
            // TODO implement fill missing values with zero
        }

        // Parse the input timeseries.
        br = StreamUtils.getBufferedReader(file);

        Map<String, TimeSeries> tsMap = new HashMap<String, TimeSeries>();
        String line = null;
        while ( (line = br.readLine()) != null) {
            String[] l = null;
            l = line.split(COLUMN_DELIMITER);
            if (l.length != 3)
                l = line.split("\\s");
            if (l.length != 3)
                throw new RuntimeException("Malformed input format: " + line);

            String k = l[0];
            long time = Long.parseLong(l[1]);
            float val = Float.parseFloat(l[2]);
            if (tsMap.containsKey(k))
                tsMap.get(l[0]).append(time, val);
            else {
                TimeSeries ts = new TimeSeries();
                ts.meta.fileName = k;
                ts.meta.id = k;
                ts.meta.name = k;
                ts.append(time,val);
                tsMap.put(k, ts);
            }
        }
        br.close();

        for (TimeSeries ts : tsMap.values()) {
            ProcessableObject po = ProcessableObjectFactory.create(ts, p);
            po.process();
        }
    }
}
