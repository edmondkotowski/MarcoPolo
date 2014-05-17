package com.app.marcopolo;

/**
* Created by cdockter on 5/17/2014.
*/
public class FileNameProvider {
    public String getFileName(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public String getRawName(String fileName) {
        return fileName.replaceAll("_", " ");
    }
}
