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
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/*
Processor to read files containing multiple, sparse time series. Possible data formats: </br>
File has to be sorted by time.
time_series_name,datetime,value</br>
time_series_name datetime value</br>
time_series_name \t datetime \t value</br>
 */

public class BatchFileInputProcessor implements InputProcessor {

    protected org.apache.logging.log4j.Logger logger;

    private String file = null;
    private String fileName = null;
    private BufferedReader br = null;

    private final static String COLUMN_DELIMITER = ",";

    public BatchFileInputProcessor(String file) {
        this.file = file;
        this.fileName = "egads_output_"+(new File(file)).getName();
    }

    public void processInput(Properties p) throws Exception {

        // Initialize output writer
        if (p.getProperty("OUTPUT") != null && p.getProperty("OUTPUT").equals("FILE")) {
            BatchFileUtils.getOrCreatePermanentOutputWriter(this.fileName);
        }

        // Make full pre-scan to find min and max time
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        Set<Long> timeStamps = new HashSet<Long>();

        // Filling missing values as this is a spare data file format.
        // The FILL_MISSING property is ignored, and all missing values are set to 0 instead

        System.out.println("Spare data formate: Filling missing values with 0");
        br = BatchFileUtils.getBufferedReader(file);
        String linep = null;
        while ((linep = br.readLine()) != null) {
            String[] l = linep.split("(\\s|,)");
            if (l.length != 3) {
                throw new RuntimeException("Malformed input format: " + linep);
            }

            long time = Long.parseLong(l[1]);
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
            timeStamps.add(time);
        }
        System.out.println("MinTime " + minTime + ", maxTime " + maxTime);

        // Parse the input timeseries.
        String line = null;
        int cnt = 0;
        long prevTime = minTime;
        TimeSeries ts = null;
        br = BatchFileUtils.getBufferedReader(file);

        while ((line = br.readLine()) != null) {
            String[] l = null;

            l = line.split(COLUMN_DELIMITER);
            if (l.length != 3) {
                l = line.split("\\s");
            }
            if (l.length != 3) {
                throw new RuntimeException("Malformed input format: " + line);
            }

            String k = l[0];
            long time = Long.parseLong(l[1]);
            float val = Float.parseFloat(l[2]);

            if (ts == null || !ts.meta.id.equals(k)) {
                if (ts != null) {
                    // Fill missing values until the last time stamp
                    for (long i = prevTime + 1; i <= maxTime; i++ ) {
                        if (timeStamps.contains(i)) {
                            ts.append(i, 0);
                        }
                    }
                    ProcessableObject po = ProcessableObjectFactory.create(ts, p);
                    po.process();
                }
                ts = new TimeSeries();
                ts.meta.fileName = k;
                ts.meta.id = k;
                ts.meta.name = k;
                prevTime = minTime;
            }

            // Fill missing values with 0's
            if (time - prevTime > 0) {
                int i = 0;
                for ( ; i < (time - prevTime); i++) {
                    if (timeStamps.contains(prevTime+i)) {
                        ts.append(prevTime+i, 0);
                    }
                }
                prevTime += i;
            }

            // Add real data
            ts.append(time, val);
            prevTime++;

            if (++cnt % 1000000 == 0) {
                System.out.print("Lines processed " + cnt++ + "\n");
                if (p.getProperty("OUTPUT") != null && p.getProperty("OUTPUT").equals("FILE")) {
                    BatchFileUtils.getOrCreatePermanentOutputWriter(this.fileName).flush();
                }
            }
        }
        // Process last timeseries
        ProcessableObject po = ProcessableObjectFactory.create(ts, p);
        po.process();

        br.close();
        if (p.getProperty("OUTPUT") != null && p.getProperty("OUTPUT").equals("FILE")) {
            BatchFileUtils.closePermanentOutputWriter();
            System.out.println("Output written to " + BatchFileUtils.outputFileName);
        }
    }
}
