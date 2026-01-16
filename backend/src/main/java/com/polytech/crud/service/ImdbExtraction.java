package com.polytech.crud.service;

import com.polytech.utils.ImdbDatasets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

@Service
public class ImdbExtraction {

    @Value("${imdb.download.path:/tmp}")
    private String downloadPath;

    public void downloadFile(String urlString, String fileName) throws Exception {
        Path targetPath = Paths.get(downloadPath, fileName);
        System.out.println("Downloading file from " + urlString + " to " + targetPath);
        URL url = new URI(urlString).toURL();
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(targetPath.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public void downloadAllDatasets() throws Exception {
        for (ImdbDatasets dataset : ImdbDatasets.values()) {
            downloadFile(dataset.getUrl(), dataset.getFileName());
        }
    }

    public void extractGzFile(String gzFileName) throws IOException {
        Path gzPath = Paths.get(downloadPath, gzFileName);
        Path outputPath = Paths.get(downloadPath, gzFileName.replace(".gz", ""));

        try (FileInputStream fis = new FileInputStream(gzPath.toFile());
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    public String getFilePath(String fileName) {
        return Paths.get(downloadPath, fileName).toString();
    }
}
