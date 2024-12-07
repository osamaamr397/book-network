package com.amr.book.file;

import com.amr.book.book.Book;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;

@Service
@Slf4j //used for logging
@RequiredArgsConstructor
public class FileStorageService {
    // i want it to be generic or i want to configure it because the file upload path might depend
    // from one environment to another so i will use @Value
    @Value("${photos-output-path}")
    private String fileUploadPath;
    public String saveFile(@NonNull MultipartFile sourceFile,

                           @NonNull Integer userId) {
        //in application.yml i initialized the file size with 50 mb

         //i know the final path of my upload file
        //but i want to create a sub path it considers that is the output path
        final String fileUploadSubPath = "users" + separator + userId; //as i want file for each user
        return uploadFile(sourceFile,fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile sourceFile,@NonNull String fileUploadSubPath) {
        final String finalUploadPath = fileUploadPath + separator + fileUploadSubPath;
        //Ex-> ./upload/users/1
        File targetFolder = new File(finalUploadPath);
        if(!targetFolder.exists()){
            boolean folderCreated =targetFolder.mkdirs();
            if(!folderCreated){
                log.warn("Failed to create the target folder");
                return null;
            }

        }
        //from this sourceFile i want to extract the file extension in order to create my target file path
        final String fileExtension = geFileExtension(sourceFile.getOriginalFilename());
        //getOriginalFilename will return the full name of the file

        //ex-> ./upload/users/1/66562626595.jpg
        String targetFilePath = finalUploadPath+separator+System.currentTimeMillis()+"."+fileExtension;
        //to create new file name
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath,sourceFile.getBytes());
            log.info("File saved to "+targetFilePath);
            return targetFilePath;
        }catch (IOException e){
            log.error("File was not saved",e);
        }
        return null;
    }

    private String geFileExtension(String filename) {
        if(filename ==null||filename.isEmpty()){
            return "";
        }
        //something.PNG
        //         | ->to get the index of the dot
        int lastDotIndex = filename.lastIndexOf(".");
        if(lastDotIndex == -1){
            return "";
        }
        //.PNG ->.png
        return filename.substring(lastDotIndex+1).toLowerCase();
    }

}
