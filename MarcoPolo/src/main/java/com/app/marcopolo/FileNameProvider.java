package com.app.marcopolo;

/**
* Created by cdockter on 5/17/2014.
*/
public class FileNameProvider {

    public static final String GROUP_PREFIX = "--group--";

    public String getFileName(String raw) {
        return GROUP_PREFIX + raw.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public String getRawName(String fileName) {
        return fileName.replace(GROUP_PREFIX, "").replaceAll("_", " ");
    }

    public boolean isFile(String fileName) {
        return fileName.startsWith(GROUP_PREFIX);
    }
}
