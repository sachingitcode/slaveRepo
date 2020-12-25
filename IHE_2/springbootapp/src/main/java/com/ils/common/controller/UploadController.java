package com.ils.common.controller;

import com.ils.util.FileSystemStorageService;
import com.ils.Model.HRefModel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UploadController {
   Logger logger = Logger.getLogger(UploadController.class);
    @Autowired
    private FileSystemStorageService storageService;
    @Autowired
      private CommonJdbcUtil commonJdbcUtil;


    @RequestMapping(value = "/files/list", method = RequestMethod.GET)
    public String listFiles(Model model) {
        List<Path> lodf = new ArrayList<>();
        List<HRefModel> uris = new ArrayList<>();

        try {
            lodf = storageService.listSourceFiles(storageService.getUploadLocation());
            for (Path pt : lodf) {
                HRefModel href = new HRefModel();
                href.setHref(MvcUriComponentsBuilder
                        .fromMethodName(UploadController.class, "serveFile", pt.getFileName().toString())
                        .build()
                        .toString());

                href.setHrefText(pt.getFileName().toString());
                uris.add(href);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model.addAttribute("listOfEntries", uris);
        return "file_list :: urlFileList";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @RequestMapping(value = "/files/upload", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileUpload(@RequestParam("photos") MultipartFile[] file,
            @RequestParam("doc_name") String docName,
            RedirectAttributes redirectAttributes) {
        String status= storageService.store(file, docName);
        logger.info("Document Name==>>"+docName);
        logger.info("handleFileUpload");
//        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        
        return status;
    }
    
    @RequestMapping(value = "/findByIdUplaodListing", headers = "Accept=application/json")
    public @ResponseBody
    String findByIdUploadListing(@RequestParam(value = "id", required = false) String id) {
        logger.info("findByIdUploadListing");
        JSONArray data = commonJdbcUtil.getQueriesForUPloadListing(id);
        logger.info("data"+data);
        return data.toString();
    }
      
    @RequestMapping(value = "/deleteIdUploadListing", headers = "Accept=application/json")
    public @ResponseBody
    String deleteIdUploadListing(@RequestParam(value = "pk", required = false) String pk,
            @RequestParam(value = "path", required = false) String path) {
        logger.info("deleteIdUploadListing");
        String data = storageService.deleteFile(pk,path);
       
        logger.info("data"+data);
        return data;
    }

}
