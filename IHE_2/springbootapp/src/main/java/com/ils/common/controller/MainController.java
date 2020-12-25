/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.common.controller;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ils.util.Common;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author maverick
 */
@Controller
public class MainController {

    Logger logger = Logger.getLogger(MainController.class);

    @Autowired
    private CommonJdbcUtil commonJdbcUtil;

    @Autowired
    private Common common;

   

    @GetMapping("/listPages")     /// apply1,apply2 etc
    public String listPages(HttpServletRequest request, HttpServletResponse response, ModelMap map, @RequestParam(value = "id", required = false) String ApplyFormId) {
        logger.info("Your id is : " + ApplyFormId);
        HttpSession session = request.getSession(false);
        session.setAttribute("listPages", ApplyFormId);
        session.setAttribute("current", null);
        session.setAttribute("previous", null);
        session.setAttribute("next", null);
        String data[] = commonJdbcUtil.getNavDetail(ApplyFormId);
        session.setAttribute("current", data[1]);
        session.setAttribute("previous", data[2]);
        session.setAttribute("next", data[3]);
        String viewpage = commonJdbcUtil.getAddPageDetail(ApplyFormId);   //addapplication1 .... viewpage
        session.setAttribute("viewpage", viewpage);
        logger.info("listPages RETURNdata is " + data[0]);
        logger.info("viewpage viewpage is " + viewpage);
        logger.info("listPages******ID " + session.getId());
//                       session.invalidate();    // check its uses 
        return data[0];
    }

    @RequestMapping(value = "/resetLoadForm", headers = "Accept=application/json")   // reserting  values of load form i.e. making a new viewpage and id
    public @ResponseBody
    void resetLoadform(HttpServletRequest request, @RequestParam(value = "id", required = false) String id) {
        HttpSession session = request.getSession(false);
        String ApplyFormId = session.getAttribute("listPages").toString();
        String viewpage = commonJdbcUtil.getAddPageDetail(ApplyFormId);   //addapplication1 .... viewpage
        session.setAttribute("viewpage", viewpage);
//        String idr = addEditcheckerValue(request, id);  
        session.setAttribute("addEditChecker", id.equals("0") ? "0" : "1");
        logger.info("viewpage  is " + viewpage + "; " + id + "::  0 for Add , 1 for Edit/update ");
    }
    //Controller

    @RequestMapping(value = "/findByIdListing", headers = "Accept=application/json")   // main list where we get headers and body by query 
    public @ResponseBody
    String findByIdListing(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        JSONObject data = commonJdbcUtil.getHeaderNValues(session.getAttribute("listPages").toString());  //requried
        logger.info(" findByIdListing data: " + data.toString());
        return data.toString();
    }
    
    
    @RequestMapping(value = "/getDetailsByDate", headers = "Accept=application/json")   // main list where we get headers and body by query 
    public @ResponseBody
    String findByIdListing(HttpServletRequest request , @RequestParam String startDate , String endDate   ) {
        HttpSession session = request.getSession(false);
        JSONObject data = commonJdbcUtil.getHeaderNValuesByDate(session.getAttribute("listPages").toString() , startDate , endDate  );  //requried
        logger.info(" findByIdListing data: " + data.toString());
        return data.toString();
    }
    
    
    
    

//    public String addEditcheckerValue(HttpServletRequest request, String id) {    // check for edit or add
//        HttpSession session = request.getSession(false);
//        logger.info(" data: " + id);
//        if (id.equals("0")) {
//            session.setAttribute("addEdit Checker", "0");     // 0 for Add , 1 for Edit
//        } else {
//            session.setAttribute("addEdit Checker", "1");  // 0 for Add , 1 for Edit
//        }
//        return id;  // we can put session.setAttribute("addEdit Checker", id )   where that func is calling 
//    }
    @GetMapping("/loadPages")
    public String loadPages(HttpServletRequest request, HttpServletResponse response, ModelMap map, @RequestParam(value = "id", required = false) String id) {
        HttpSession session = request.getSession(true);  // SESSION CREATED
        logger.info("Your id is : " + id);  // row id   
        String viewpage = session.getAttribute("viewpage").toString();       //addapplication1
        logger.info("viewpage at loadPages= " + viewpage);         //        String viewpage = commonJdbcUtil.getAddPageDetail(listpage);   //addapplication1 .... viewpage   it is avalable in listpages
        String[] data = commonJdbcUtil.getNavDetail(viewpage);
        for (String dt : data) {
            logger.info("data==++++++>>" + dt);
        }
        String pageName = data[0];
        logger.info(" next ID data==++++++>>" + data[3]);    //addApplication2 
        logger.info(" currentId ID PAGE LINK==++++++>>" + data[0]);   // pages/application/addApplication1
        logger.info("  currentpageName ID data==++++++>>" + data[1]);          // addApplication1
        session.setAttribute("current", data[1]);
        session.setAttribute("previous", data[2]);
        session.setAttribute("next", data[3]);
        session.setAttribute("viewpage", data[3]);                     // if to use Next loadpages   addapplication2
        session.setAttribute("id", id);   //used to check the difference
        logger.info("loadPages*********ID " + session.getId());
        String title = commonJdbcUtil.gettitleNameBypageId(data[1]);     // get page title
        logger.info(" next ID data==++++++>>" + title);    //StudentApplicatoin FOrm 1
        session.setAttribute("title", title);
        map.addAttribute("id", id);    // 
        return pageName;
    }

    @RequestMapping(value = "/saveData", headers = "Accept=application/json") // Save data
    public @ResponseBody
    String saveData(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map, @RequestBody ObjectNode json) {
        JsonNode userData = json.get("userData");
        JSONObject autogeneratedId = new JSONObject();
        String pageid = session.getAttribute("current").toString();
        logger.info("id bysession" + session.getAttribute("id").toString());
//        String id = json.get("id").asText();
        String id = session.getAttribute("id").toString();
        logger.info(" userData " + userData + "pageid " + pageid + " id " + id + " addEdit Checker " + session.getAttribute("addEditChecker"));
        String autogenreatedTid = null;
        String tableName = commonJdbcUtil.getTableNameformId(pageid);
        if (session.getAttribute("addEditChecker").toString().equals("1")) {      //update .... Edit      
            String status = commonJdbcUtil.updateDataUtil(userData, tableName, id);      // update by id   
        } else {         // insert  ,,,,Add 
            logger.info(" " + pageid);
            autogeneratedId = commonJdbcUtil.saveDataUtil(userData, tableName, id, session);
            autogenreatedTid = autogeneratedId.getString("r_id");
            if (id.equals("0")) {
                id = autogenreatedTid;
            }
            logger.info("aID:  " + autogeneratedId + " aTID: " + autogenreatedTid);
        }
        map.addAttribute("id", id);
        return id.toString();
    }

    @RequestMapping(value = "/findById", headers = "Accept=application/json")    //all form created 
    public @ResponseBody
    String findById(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String pageid = session.getAttribute("current").toString();
        String id = session.getAttribute("id").toString();
        logger.info(" id " + id + "  pageid  " + pageid);
        String data = commonJdbcUtil.findByRId(commonJdbcUtil.getTableNameformId(pageid), id);
        logger.info(data);
        return data;
    }

    @RequestMapping(value = "/deleteIdListing")   // deleteing the 
    public @ResponseBody
    String deleteidListing(ModelMap map, HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam(value = "id", required = false) String id) {
        String viewpage = session.getAttribute("viewpage").toString(); //
        String current = session.getAttribute("current").toString();
        logger.info("viewpage param deleteidListing: " + viewpage);
        logger.info("current param deleteidListing: " + current);
        logger.info("idddddd deleteidListing: " + id);
        String status = null;
        if (viewpage.equals("m2mAddForm")) {
            logger.info(" deleteidList from m2mAddForm: ");
            status = commonJdbcUtil.m2mAddForm(id, viewpage);
        } else if (viewpage.equals("masterAddForm")) {
            logger.info(" deleteidList from masterAddForm: ");
            status = commonJdbcUtil.masterAddForm(id, viewpage);
        } else {
            status = commonJdbcUtil.deleteIdlisting(id, viewpage);
        }
        return current;     // jugaad 
    }

//    
    @RequestMapping(value = "/loadPrevForm", headers = "Accept=application/json")  //loasdinf previous form page
    public String loadPrevForm(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map, @RequestParam(value = "id", required = false) String id) {
//       String viewpage = session.getAttribute("viewpage").toString(); //
        String current = session.getAttribute("current").toString();
        String previous = session.getAttribute("previous").toString();
        logger.info("previous  loadPrevForm: " + previous);
        String[] data = commonJdbcUtil.getNavDetail(previous);
        session.setAttribute("current", data[1]);
        session.setAttribute("previous", data[2]);
        session.setAttribute("next", data[3]);
        session.setAttribute("viewpage", data[3]);
        session.setAttribute("addEditChecker", "1");
        logger.info("previous   RETURN PAGE loadPrevForm: " + data[0]);
        return data[0];
    }

    @RequestMapping(value = "/saveMasterData", headers = "Accept=application/json")    // masters  page
    public @ResponseBody
    String saveMasterData(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map, @RequestBody ObjectNode json) {
        JsonNode userData = json.get("userData");
        String master = json.get("master").asText();
        String status = "";
        logger.info("saveMasterData>>>>>>>userData " + userData);
        logger.info("saveMasterData>>>>>>>master " + master);
        logger.info("saveMasterData>>>>>>>userData.size() " + userData.size());
        String id = session.getAttribute("id").toString();
//        logger.info("saveMasterData>>>>>>>ID " + id);
        logger.info("saveMasterData>>>>>>>SidID " + id);
        String master_type_id = commonJdbcUtil.saveMasterDataUtil(userData, master, id);
        String[] arr = new String[userData.size()];
        for (int i = 0; i < userData.size(); i++) {
            arr[i] = userData.get(i).get("values").toString().replace('"', ' ');  // changwe "values" accordingly
            commonJdbcUtil.saveMasterValueDataUtil(arr[i], master_type_id);
            logger.info("saveMasterData>>>>>>>arr[i]  " + arr[i]);
        }
        logger.info("saveMasterData>>>>>>>master " + master);
        return status.toString();
    }

//    //        commonJdbcUtil.saveTestDataUtil(taxperc);  // 
    @RequestMapping(value = "/saveTestData", headers = "Accept=application/json")     // testing Purpose
    public @ResponseBody                                                                 // saveTestData  page
    String saveTestData(HttpServletRequest request, HttpSession session, ModelMap map, @RequestBody ObjectNode json) throws Exception {
        JsonNode userData = json.get("userData");
        logger.info(" viewpage  :" + session.getAttribute("viewpage"));
        logger.info("current is :" + session.getAttribute("current"));
        String POST_PARAMS = userData.toString();
        logger.info(POST_PARAMS);
        JSONObject jsOb = null;
        String methodName = commonJdbcUtil.getPrePostFilter(session.getAttribute("current").toString());
//        if (methodName != null && methodName != "") {
//            logger.info("#$#$#$#$$#$#$#$#$#$ " + methodName);
//            jsOb = getPreHandle r (methodName, POST_PARAMS);
//        }
//        commonJdbcUtil.saveTestDataUtil( request,jsOb.toString());
        commonJdbcUtil.saveTestDataUtil(request, "655");

        String abc = null;
        String xyz = null;

        String postHndl = getPostHandler(session, abc, xyz);

        return "true";
    }

    private String getPostHandler(HttpSession session, String abc, String xyz) {    // postHandler

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JSONObject getPreHandler(String handlerUrl, String jsonVals) throws Exception {
        logger.info("PreHandler MainController ");
        URL obj = new URL(handlerUrl);
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("userId", "a1bcdefgh");   // GET BY header
        postConnection.setRequestProperty("Content-Type", "application/json");
        postConnection.setDoOutput(true);
        OutputStream os = postConnection.getOutputStream();
        os.write(jsonVals.getBytes());
        os.flush();
        os.close();
        InputStream is = postConnection.getInputStream();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
        String responseData = "";
        if ((responseData = responseReader.readLine()) != null) {
            System.out.append("Response Json = " + responseData);
        }
        JSONObject jsonObject = new JSONObject(responseData);
        return jsonObject;
    }

    @RequestMapping(value = "/findMastersById", headers = "Accept=application/json")   //EDIT MASTERS
    public @ResponseBody
    String findMastersByid(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String pageid = session.getAttribute("current").toString();
        String id = session.getAttribute("id").toString();
        logger.info("ffindMastersByidffindMastersByid  idBysesssion==>>  " + id + "  pageidid in findMastersByid==>>  " + pageid);
        String data = commonJdbcUtil.getMastersValuesById(id);          // change it and use it for rId only as we cchnge in savedata
        logger.info(" response object == " + data);
        return data;
    }

    @RequestMapping(value = "/saveM2Mdata", headers = "Accept=application/json")   // M2M page
    public @ResponseBody
    String saveM2Mdata(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map, @RequestBody ObjectNode json) {
        JsonNode userData = json.get("userData");
        String status = "";
        String masterid = userData.get("masterid").toString();
        String submasterid = userData.get("submasterid").toString();
        String mastervalueid = userData.get("mastervalueid").toString();
        logger.info("saveM2Mdata>>>>>>>userData " + userData);
        logger.info("saveM2Mdata>>>>>>>masterid " + masterid);
        logger.info("saveM2Mdata>>>>>>>submasterid " + submasterid);
        logger.info("saveM2Mdata>>>>>>>mastervalueid " + mastervalueid);
        commonJdbcUtil.M2Mmasterlink(masterid, submasterid);
        logger.info("saveM2Mdata>>>>>>>ar343 " + userData.get("submastervalues").size());
        String[] arr = new String[userData.get("submastervalues").size()];
        for (int i = 0; i < userData.get("submastervalues").size(); i++) {
            arr[i] = userData.get("submastervalues").get(i).toString().replace('"', ' ');
            commonJdbcUtil.M2MmasterValuelink(mastervalueid, arr[i]);
            logger.info("saveM2Mdata>>>>>>> arr[ijkl] " + arr[i]);
        }
        return status.toString();
    }

    @RequestMapping(value = "/navData", headers = "Accept=application/json")     // for listing i.e apply,aplly1 ,apply2
    public @ResponseBody
    String navData(@RequestParam(value = "id", required = false) String id) {
        JSONArray data1 = commonJdbcUtil.getNavData(id);
        logger.info("Method: navData");
        logger.info("navData param id: " + id);
        logger.info("navData00 param data1: " + data1);
        return data1.toString();
    }

    @RequestMapping(value = "/commonQuery", headers = "Accept=application/json")    // it is used once,, can b removed  same as navDATA
    public @ResponseBody
    String commonQuery(@RequestParam(value = "id", required = false) String id , HttpServletRequest request, HttpSession session) {
       
         String userName = session.getAttribute("userName").toString();
          logger.info(" id: " + id + " .. userName.." + userName);
        String data = commonJdbcUtil.getBycommonQuery(id);
        logger.info(" Query :" + data);
        return data.toString();
    }

    @RequestMapping(value = "/docListing", headers = "Accept=application/json")
    public @ResponseBody
    String docListing(@RequestParam(value = "id", required = false) String id) {
        JSONArray data1 = commonJdbcUtil.getDocuments(id);
        logger.info("Request param id: " + id);
        return data1.toString();
    }
    
       @RequestMapping(value = "/occupationListing", headers = "Accept=application/json")
    public @ResponseBody
    String occupationListing() {
        String id = "occupation";
         logger.info("List Master"  ) ;
        JSONArray data1 = commonJdbcUtil.getValuesByMaster(id);
        return data1.toString();
    }
    
    
    @RequestMapping(value = "/getMasterType", headers = "Accept=application/json")       // Getting masters for editM2M
    public @ResponseBody
    String getMasterType() {
        JSONArray data = commonJdbcUtil.getMasterType();
        logger.info("Method: getMasterType" + data);
        return data.toString();
    }

    @RequestMapping(value = "/getValueOfMaster", headers = "Accept=application/json")    //values of Master for M2M ie. Country-> USA, india ; state-UP ,Tokyo
    public @ResponseBody
    String getValueOfMaster(@RequestParam(value = "id", required = false) String id) {    // id of country
        JSONArray data = commonJdbcUtil.getValueOfMaster(id);
        logger.info("Method: getMasterType" + data);
        return data.toString();
    }

    @RequestMapping(value = "/getEditM2Mvalues", headers = "Accept=application/json")    // get values for edit M2M  psge
    public @ResponseBody
    String getEditM2Mvalues(HttpServletRequest request, HttpServletResponse response, HttpSession session) {    // id of country
        String id = session.getAttribute("id").toString();
        String data = null;
        if (id != "0") {
            data = commonJdbcUtil.getEditM2Mvaluesutil(id);
        }
        logger.info("Method: getMasterType" + data);
        return data;
    }

    @RequestMapping(value = "/getAge", headers = "Accept=application/json")
    public @ResponseBody
    String getAge(@RequestParam(value = "date", required = false) String date) {
        logger.info("getAge: ");
        String data = common.getAge(date);
        logger.info("Your age is : " + data);
        logger.info("Method: findByOcc");
        logger.info("Request param id: ");
        return data.toString();
    }

}

//  @RequestMapping(value = "/findByIdView", headers = "Accept=application/json")
//    public @ResponseBody
//    String findByIdView(@RequestParam(value = "id", required = false) String id, @RequestParam String pageid) {
//        logger.info("Method: findByIdView");
//        logger.info("Request param id: " + id);
//        logger.info("Request param pageid: " + pageid);
//        String data = commonJdbcUtil.getQueriesForView(pageid, id);
//        return data;
//    }
//    @RequestMapping(value = "/getMasterType", headers = "Accept=application/json")    // it is used once,, can b removed
//    public @ResponseBody
//    String getMasterType(@RequestParam(value = "id", required = false) String id) {
//        JSONArray data = commonJdbcUtil.getMasterTypeUtil(id);
//        logger.info("Method: getMasterType" + data);
//        return data.toString();
//    }
//    @RequestMapping(value = "/deleteIdListing")
//    public @ResponseBody
//    String deleteidListing(@RequestParam String id, @RequestParam String pageid) {
//        String status = null;
//        status = commonJdbcUtil.deleteIdlisting(id, pageid);
//        return status;
//    }
//
//    @RequestMapping(value = "/navData", headers = "Accept=application/json")
//    public @ResponseBody
//    String navData(@RequestParam(value = "id", required = false) String id) {
//        JSONArray data1 = commonJdbcUtil.getNavData(id);
//        logger.info("Method: navData");
//        logger.info("Request param id: " + id);
//        return data1.toString();
//    }
//
//    @RequestMapping(value = "/findByListConfig", headers = "Accept=application/json")
//    public @ResponseBody
//    String findByListConfig(@RequestParam(value = "id", required = false) String id) {
//        JSONObject data = commonJdbcUtil.getListConfig(id);
//        logger.info("Method: findByIdListing");
//        logger.info("Request param id: " + id);
//        return data.toString();
//    }
//
//    public String deleteFile(String id, String jdoc) {
//        String data = commonJdbcUtil.get QueriesForDeleteListing(id);
//        File file = new File(jdoc);
//        if (file.exists()) {
//            if (file.delete()) {
//                logger.info("File deleted successfully");
//            } else {
//                logger.info("Fail to delete file");
//            }
//        }
//        return data;
//    }
//
//    @RequestMapping(value = "/getAge", headers = "Accept=application/json")
//    public @ResponseBody
//    String getAge(@RequestParam(value = "date", required = false) String date) {
//        logger.info("getAge: ");
//        String data = common.getAge(date);
//        logger.info("Your age is : " + data);
//        logger.info("Method: findByOcc");
//        logger.info("Request param id: ");
//        return data.toString();
//    }
//
//}
// for insrting in query
//        String status = "";
//        String taxperc = "";
//        logger.info("saveTestData>>>>>>>userData " + userData);
//        logger.info("saveTestData>>>>>>>userData.size() " + userData.size());
//        String str = userData.get("tax").toString().replace('"', ' ').trim();
//        int val = Integer.parseInt(str);
//        if (val < 10000) {
//            taxperc = "10";
//        } else if (val > 10000 && val <= 20000) {
//            taxperc = "20";
//        } else if (val > 20000 && val <= 30000) {
//            taxperc = "30";
//        } else {
//            taxperc = "40";
//        }
//        commonJdbcUtil.saveTestDataUtil(taxperc);
//        return status.toString();
//        String status = "";
//        String taxperc = "";
//        logger.info("saveTestData>>>>>>>userData " + userData);
//        logger.info("saveTestData>>>>>>>userData.size() " + userData.size());
//        String str = userData.get("tax").toString().replace('"', ' ').trim();
//        int val = Integer.parseInt(str);
//        if (val < 10000) {
//            taxperc = "10";
//        } else if (val > 10000 && val <= 20000) {
//            taxperc = "20";
//        } else if (val > 20000 && val <= 30000) {
//            taxperc = "30";
//        } else {
//            taxperc = "40";
//        }
//        commonJdbcUtil.saveTestDataUtil(taxperc);
//        return status.toString();
//        User user = restTemplate.getForEntity(
//                "/apiA/user",
//                User.class
//        ).getBody();
//
//        int requestCounter = 0 ;
//        HttpStatus responseStatus =  null;
//        while (responseStatus != OK || ++ requestCounter == 3) {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    "/apiB/user",
//                    HttpMethod.POST,
//                    user,
//                    String.class
//            );
//            responseStatus = response.getStatusCode();
//        }
// responseDAta is string(json)    converted into map 
// ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mapp = mapper.readValue(responseData, Map.class);
//        logger.info("Mppppp " + mapp);
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mapp = mapper.readValue(responseData, Map.class);
//        logger.info("Mppppp " + mapp);
//        String code = mapp.get("name");

