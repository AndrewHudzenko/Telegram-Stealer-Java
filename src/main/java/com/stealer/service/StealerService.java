package com.stealer.service;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface StealerService {
    void copyFolder(File sourceFolder, File destinationFolder) throws IOException;
    void copyFile(File sourceFile, File destinationFile) throws IOException;
    void zipFolder(String[] sourceFolders, String destinationZipFile);
    void addFolderToZip(String baseFolderPath, File folder, ZipOutputStream zip) throws IOException;
    void addFileToZip(String folderPath, File file, ZipOutputStream zip) throws IOException;
    void deleteFolder(File folder);
}
