package com.polytech.crud.service;

import com.polytech.utils.ImdbDatasets;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class ImdbExtraction {
    public static void downloadFile(String urlString, String filePath) throws Exception {
        System.out.println("Downloading file from " + urlString + " to " + filePath);
        URL url = new URI(urlString).toURL();
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void downloadAllDatasets() throws Exception {
        for (ImdbDatasets dataset : ImdbDatasets.values()) {
            downloadFile(dataset.getUrl(), dataset.getFileName());
        }
    }

    public static void extractGzFile(String gzFileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(gzFileName);
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(gzFileName.replace(".gz", ""))) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
}
