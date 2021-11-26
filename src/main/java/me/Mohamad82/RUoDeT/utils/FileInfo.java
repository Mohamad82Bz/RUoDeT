package me.Mohamad82.RUoDeT.utils;

import java.io.File;

public class FileInfo {

    private final long lastModified;
    private final long lenght;

    private FileInfo(long lastModified, long lenght) {
        this.lastModified = lastModified;
        this.lenght = lenght;
    }

    public static FileInfo fileInfo(long lastModified, long lenght) {
        return new FileInfo(lastModified, lenght);
    }

    public static FileInfo fileInfo(File file) {
        return new FileInfo(file.lastModified(), file.length());
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLenght() {
        return lenght;
    }

}
