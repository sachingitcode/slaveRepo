package com.ils.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author maverick
 */
import com.ils.common.controller.CommonJdbcUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FileSystemStorageService {
    Logger logger = Logger.getLogger(FileSystemStorageService.class);
    private Path uploadLocation;

    @Autowired
    CommonJdbcUtil commonJdbcUtil;

    @PostConstruct
    public void init() {
        this.uploadLocation = Paths.get(Constants.UPLOAD_LOCATION);
        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String store(MultipartFile[] files, String docName) {
        Common common = new Common();
        String status = "";
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Path path;
            String fileExtension = "";
            String filename = StringUtils.cleanPath(file.getOriginalFilename());

            try {
                if (file.isEmpty()) {
                    throw new RuntimeException("Failed to store empty file " + filename);
                }

                // This is a security check
                if (filename.contains("..")) {
                    throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
                }

                try (InputStream inputStream = file.getInputStream()) {
                    path = this.uploadLocation.resolve(filename);
                    fileExtension = common.getFileExtension(path.toString());
                    logger.info("path==>>" + path + "docName==>>" + docName + "fileName==>>" + filename + "fileExtension==>>" + fileExtension);
                    Files.copy(inputStream, this.uploadLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                    status = commonJdbcUtil.saveFile(docName, filename, path.toString(), fileExtension);
                }
            } catch (IOException e) {
                status = "false";
                throw new RuntimeException("Failed to store file " + filename, e);
            }
        }
        return status;

    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = uploadLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    public List<Path> listSourceFiles(Path dir) throws IOException {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{txt}")) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public Path getUploadLocation() {
        return uploadLocation;
    }

    public String deleteFile(String pk,String path) {
        String data = commonJdbcUtil.getQueriesForDeleteUPloadListing(pk);
        File file=new File(path);
		if(file.exists()){
			if(file.delete()){
				logger.info("File deleted successfully");
			}else{
				logger.info("Fail to delete file");
			}
		}
        return data;
    }
}
