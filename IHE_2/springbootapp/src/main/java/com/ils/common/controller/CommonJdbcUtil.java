/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.common.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 *
 * @author
 */
public class CommonJdbcUtil {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;

    final KeyHolder keyHolder = new GeneratedKeyHolder();
    Logger logger = Logger.getLogger(CommonJdbcUtil.class);

    public JSONObject saveDataUtil(JsonNode data, String tableName, String id, HttpSession session) {   //Edit
        JSONObject jsondata = new JSONObject();
        logger.info("tid " + id);
        String QueryString = "INSERT INTO " + tableName + " (jdoc,r_id ,created_by,created_date) values(?,?,1,now())"; // sekect ka b dekho b bhai
        logger.info("OK saveDatasaveDataQuery" + QueryString);
        logger.info("OK JsonNode data for saveDataUtil" + data.toString());
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(QueryString, new String[]{"id"});
                    ps.setString(1, data.toString());
                    ps.setString(2, id);
                    return ps;
                }, keyHolder);
        logger.info(" printing key ==> " + keyHolder.getKey());
        String r_id = keyHolder.getKey().toString();  // id of that row
        if (id.equals("0")) {
            logger.info("  Undefinez Vals");
            String queryListing = " UPDATE " + tableName + " SET r_id = ? WHERE id = ?";
            jdbcTemplate.update(queryListing, new Object[]{r_id, r_id});   // chceck if it has tid or     
            session.setAttribute("id", r_id);      //
        }
        jsondata.put("id", id);
        jsondata.put("r_id", r_id);
        jsondata.put("status", "true");
        return jsondata;
    }

    @Cacheable(value = "getAddPageDetail", key = "#id")
    public String getAddPageDetail(String id) {
        logger.info("id for getNAvdetails: " + id);
        return jdbcTemplate.query("SELECT addform FROM PAGE_MASTER WHERE id=?", new Object[]{id}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("addform");
                logger.info("values for getAddPageDetail: " + data);
            }
            return data;
        });
    }

//    @Cacheable(value = "getNavDetail", key = "#id")
    public String[] getNavDetail(String id) {
        logger.info("id for getNAvdetails: " + id);
        return jdbcTemplate.query("SELECT id, path, previous, next FROM PAGES WHERE id=?", new Object[]{id}, (ResultSet rs) -> {
            String data[] = new String[4];
            while (rs.next()) {
                data[0] = rs.getString("path");
                data[1] = rs.getString("id");
                data[2] = rs.getString("previous");
                data[3] = rs.getString("next");
            }
            logger.info("values for getNavDetail: " + data);
            logger.info("values for if: " + data);
            return data;

        });
    }

    public List<String[]> getQueries(String id) {
        logger.info("id for Query: " + id);
        List<String[]> results = new ArrayList<String[]>();
        String sql = "SELECT "
                + " QUERIES.pk,"
                + " QUERIES.`query`,"
                + " QUERIES.type,"
                + " QUERIES_PAGES.pages_pk,"
                + " PAGES.table"
                + " FROM PAGES "
                + " INNER JOIN QUERIES_PAGES ON QUERIES_PAGES.pages_pk = PAGES.pk"
                + " INNER JOIN QUERIES on QUERIES.pk=QUERIES_PAGES.queries_pk"
                + " AND QUERIES.type != 'U' "
                + " where PAGES.id= ?"
                + " ORDER BY QUERIES.order";
        logger.info("sqlQUERY for getQueries: " + sql);
        results = jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
            List<String[]> result = new ArrayList<>();
            while (rs.next()) {
                String[] oneResult = new String[5];
                oneResult[0] = rs.getString("pk");
                oneResult[1] = rs.getString("query");
                oneResult[2] = rs.getString("type");
                oneResult[3] = rs.getString("pages_pk");
                oneResult[4] = rs.getString("table");
                result.add(oneResult);
            }
            return result;
        });
        logger.info("values for getQueries: " + results);
        return results;
    }

    @Cacheable(value = "getUpdateQuerie", key = "#id")
    public String[] getUpdateQuerie(String id) {
        logger.info("id: " + id);
        String sql = "SELECT \n"
                + " QUERIES.`query`,\n"
                + " PAGES.table \n"
                + " from PAGES \n"
                + " INNER JOIN QUERIES_PAGES ON QUERIES_PAGES.pages_pk = PAGES.pk\n"
                + " INNER JOIN QUERIES on QUERIES.pk=QUERIES_PAGES.queries_pk "
                + " where PAGES.id= ? \n"
                + " AND type='U'";
        return jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
            String data[] = new String[2];
            while (rs.next()) {
                data[0] = rs.getString("query");
                data[1] = rs.getString("table");
            }
            return data;
        });

    }

//    @CacheEvict(value = "findById", key = "{#tabelName, #pageid}", allEntries = true)
    public String updateDataUtil(JsonNode userData, String tableName, String id) {
//        String QueryString = "UPDATE " + tableName + " SET JDOC=?  WHERE r_id = ?"; //  earlier only for update
        String QueryString = "INSERT INTO " + tableName + " ( jdoc , r_id)  VALUES (?,?)  ON DUPLICATE KEY UPDATE jdoc = ?";      // insertUpdate
        String status = "true";
        logger.info("Ok QueryString for updateDataUtil: " + QueryString + "r_id for updateDataUtil: " + id);
        try {
            jdbcTemplate.update(QueryString, new Object[]{userData.toString(), id, userData.toString()});
        } catch (Exception e) {
            status = "false";
            e.printStackTrace();
        }
        logger.info("Ok  status for updateDataUtil " + status);
        return status;
    }

    public String InsertData(String tablename, JsonNode json, HttpSession session) {
        String status = "true";
        String QueryString = "INSERT INTO " + tablename + " (jdoc,r_id ,created_by,created_date) values(?,?,1,now())"; // sekect ka b dekho b bhai
        try {
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(QueryString, new String[]{"id"});
                        ps.setString(1, json.toString());
                        ps.setString(2, session.getAttribute("referenceId").toString());
                        return ps;
                    }, keyHolder);
            session.setAttribute("referenceId", keyHolder.getKey().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            status = "false";
        }
        logger.info("Ok  status for InsertData " + status);
        return status;
    }

//    @Cacheable(value = "findById", key = "{#tabelName, #id}")
    public String findById(String tableName, String id) {
        String queryList = "SELECT JSON_OBJECT( 'id', $tablename.id, 'dt', $tablename.jdoc ) as jdoc FROM $tablename WHERE  $tablename.id=? ";
        String QueryString = queryList.replace("$tablename", tableName);
        logger.info("Ok findById: " + QueryString);
        return jdbcTemplate.query(QueryString, new Object[]{id}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("jdoc");
            }
            return data;
        });
    }

    public String findByRId(String tableName, String id) {
//        String queryway = "SELECT JSON_OBJECT( 'id', $tablename.id, 'dt', $tablename.jdoc ) as jdoc FROM $tablename WHERE  $tablename.r_id=? ";
        String queryway = "SELECT JSON_OBJECT( 'id', $tablename.id, 'dt', $tablename.jdoc ) as jdoc FROM $tablename WHERE  $tablename.r_id=? ";
        String QueryString = queryway.replace("$tablename", tableName);
        logger.info(QueryString + "  ID = " + id);
        return jdbcTemplate.query(QueryString, new Object[]{id}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("jdoc");
            }
            return data;
        });
    }

    public JSONObject getHeaderNValues(String id) {    //Impl
        logger.info("getHeaderNValues: " + id);
        JSONObject data = new JSONObject();
        String sql = "SELECT\n"
                + "LISTING.pk,\n"
                + "LISTING.header,\n"
                + "LISTING.`query`\n"
                + "FROM\n"
                + "LISTING\n"
                + "WHERE LISTING.id= ?";
        logger.info("{Query}: " + sql);
        return jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
            while (rs.next()) {
                data.put("header", new JSONArray(rs.getString("header")));      //match header and json names in listing query 
                data.put("values", findByIdListing(rs.getString("query")));
            }
            return data;
        });
    }

//    @Cacheable(value = "getQueriesForView", key = "#id")
    public String getQueriesForView(String id, String tid) {
        logger.info("id for getQueriesForView: " + id);
        String tablename = getTableNameformId(id);
//        String sql = "select JSON_OBJECT('Name',$tablename.jdoc -> '$.name',\n"
//                + "'Date of Birth',$tablename.jdoc -> '$.Dob',\n"
//                + "'Mobile no',$tablename.jdoc -> '$.mobile',\n"
//                + "'Email',$tablename.jdoc -> '$.email')\n"
//                + "as jdoc\n"
//                + "FROM $tablename\n"
//                + "WHERE $tablename.id = $id";
        String sql = "select jdoc as jdoc from  $tablename WHERE $tablename.id = $id";
        String QueryString = sql.replace("$tablename", tablename).replace("$id", tid);
//          String QueryString = sql.replace("$id", id);
        String data = null;
        data = findByIdView(QueryString);
        return data;
//        return jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
//            while (rs.next()) {
//                data = findByIdView(rs.getString("query"));
//            }
//        });
    }

    public JSONArray findByIdListing(String query) {
        logger.info("OK Query : " + query);
        JSONArray array = new JSONArray();
        return jdbcTemplate.query(query, (ResultSet rs) -> {
            while (rs.next()) {
                array.put(new JSONObject(rs.getString("jdoc")));
            }
            return array;
        });
    }

    public String findByIdView(String query) {
        logger.info("OK Query: " + query);
        return jdbcTemplate.query(query, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("jdoc");
            }
            return data;
        });
    }

    //Save File Details
    public String saveFile(String docName, String filename, String path, String fileExtension) {
        String status = "true";
        String tableName = "DOCS";
        String query = "INSERT INTO $tablename (doc_name, file_name, path, created_by, created_date, extension) VALUES (?, ?, ?, 1, now(), ?)";
        String QueryString = query.replace("$tablename", tableName);
        try {
            jdbcTemplate.update(QueryString, new Object[]{docName, filename, path, fileExtension});
        } catch (Exception e) {
            status = "false";
            e.printStackTrace();
        }
        return status;
    }

    public JSONArray getQueriesForUPloadListing(String id) {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + " JSON_OBJECT('pk' , DOCS.pk,\n"
                + "'doc_name',DOCS.doc_name,\n"
                + "'created_date',DATE_FORMAT(DOCS.created_date,\"%d-%m-%y,%h:%i:%s\"),\n"
                + "'path',DOCS.path, 'file_name',DOCS.file_name) as data\n"
                + "FROM\n"
                + "	DOCS ";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            return data;
        });
    }

    public String getQueriesForDeleteUPloadListing(String pk) {
        String tableName = "DOCS";
        String status = "true";
        String query = "DELETE FROM $tablename Where pk=?";
        String QueryString = query.replace("$tablename", tableName);
        try {
            jdbcTemplate.update(QueryString, new Object[]{pk});
        } catch (Exception e) {
            status = "false";
            e.printStackTrace();
        }
        return status;
    }

    public JSONArray getDocuments(String id) {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + " JSON_OBJECT('pk' , DOCS.pk,\n"
                + "'doc_name',DOCS.doc_name) as data\n"
                + "FROM\n"
                + "	DOCS ";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            return data;
        });

    }

    public JSONArray getNavData(String id) {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + " JSON_OBJECT('pk' ,PAGE_MASTER.pk,\n"
                + "'link_name',PAGE_MASTER.link_name,\n"
                + "'url',PAGE_MASTER.url,\n"
                + "'id',PAGE_MASTER.id) as data\n"
                + "FROM\n"
                + "PAGE_MASTER";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            logger.info("getNavDataComm sql" + sql);
            return data;
        });
    }

    public JSONObject getListConfig(String id) {
        JSONObject data = new JSONObject();
        String sql = "SELECT JSON_OBJECT(\n"
                + "'add' , LINKING_ROLE_MAPPING.add,\n"
                + "'edit' , LINKING_ROLE_MAPPING.edit,\n"
                + "'delete' , LINKING_ROLE_MAPPING.delete,\n"
                + "'view' , LINKING_ROLE_MAPPING.view) as jdoc\n"
                + "FROM LINKING_ROLE_MAPPING\n"
                + "WHERE  LINKING_ROLE_MAPPING.pk=1 ";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {

                data.put("values", new JSONObject(rs.getString("jdoc")));
            }
            return data;
        });
    }

    public String getQueriesForDeleteListing(String id) {
        String tableName = "TABLE_DUM";
        String status = "true";
        String query = "DELETE FROM $tablename Where id=?";
        String QueryString = query.replace("$tablename", tableName);
        try {
            jdbcTemplate.update(QueryString, new Object[]{id});
        } catch (Exception e) {
            status = "false";
            e.printStackTrace();
        }
        return status;
    }

//    public JSONArray getBoards(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk', BOARD_MASTER.pk, 'board_name', BOARD_MASTER.`board_ name` ) AS DATA \n"
//                + "FROM\n"
//                + "	BOARD_MASTER";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//    }
//
    public JSONArray getUniversity(String masterDetails) {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + "	JSON_OBJECT( 'pk', UNIVERSITY_MASTER.`pk`,\n"
                + "	'university_name', \n"
                + "	UNIVERSITY_MASTER.`university_name`\n"
                + "	) AS DATA \n"
                + "FROM\n"
                + "	UNIVERSITY_MASTER";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            return data;
        });

    }

    public JSONArray getValuesByMaster(String masterDetails) {
        JSONArray data = new JSONArray();
        String   sql = " select JSON_OBJECT( 'id', MASTER_VALUE.`id`,  'value' ,  MASTER_VALUE.value       ) AS DATA"
                + " from MASTER_VALUE "
                + "where master_type_id = ( select id  from MASTER_TYPE where type = 'University')";

        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            return data;
        });

    }

//    public JSONObject getOccupationF(String id) {
//        JSONObject data = new JSONObject();
//        String sql = "SELECT DISTINCT JSON_UNQUOTE(jdoc -> \"$.occupation\") as occupation from T1 ";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//
//                data.put("values", new JSONObject(rs.getString("occupation")));
//            }
//            return data;
//        });
//    }
//
//    public JSONArray getCourseListing(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk', COURSE_NAME.`pk`,\n"
//                + "	'name_course', \n"
//                + "	COURSE_NAME.`name_course`\n"
//                + "	) AS DATA \n"
//                + "FROM\n"
//                + "	COURSE_NAME";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//
//    }
////
//    public JSONArray getStream(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk', STREAM_MASTER.pk, 'name_stream',STREAM_MASTER.`name_stream` ) AS DATA \n"
//                + "FROM\n"
//                + "	STREAM_MASTER";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//    }
//
//    public JSONArray getCourse(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk', COURSE_MASTER.pk, 'name_coursea', COURSE_MASTER.`name_coursea` ) AS DATA \n"
//                + "FROM\n"
//                + "	 COURSE_MASTER";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//    }
//
//    public JSONArray getSchoolS(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk',SCHOOL_STREAM.pk, 'name_sstream', SCHOOL_STREAM.`name_sstream` ) AS DATA \n"
//                + "FROM\n"
//                + "	 SCHOOL_STREAM";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//    }
    public JSONArray getMasterType() {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + "	JSON_OBJECT( 'id', id, 'type', `type` ) AS DATA \n"
                + "FROM\n"
                + "	 MASTER_TYPE";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            logger.info("Ok  getMasterType ById " + data);
            return data;
        });
    }

//
//
//    public JSONArray getListt(String id) {
//        JSONArray data = new JSONArray();
//        String sql = "SELECT\n"
//                + "	JSON_OBJECT( 'pk',MOTH_OCC.pk, 'moth_occ', MOTH_OCC.`moth_occ` ) AS DATA \n"
//                + "FROM\n"
//                + "	 MOTH_OCC";
//        return jdbcTemplate.query(sql, (ResultSet rs) -> {
//            while (rs.next()) {
//                data.put(new JSONObject(rs.getString("data")));
//            }
//            return data;
//        });
//    }
    public boolean checkPageAvailablity(String pageid) {
        String query = "SELECT ID FROM `PAGES`where PAGES.next =?";
        return jdbcTemplate.query(query, new Object[]{pageid}, (ResultSet rs) -> {
            boolean status = false;
            while (rs.next()) {
                status = true;
            }
            return status;
        });
    }

    public String getIDforUndefined(String id) {
        String queryList = "SELECT 	id FROM	$tablename  ORDER BY id DESC	limit 1 ";
        String tabName = getTableNameformId(id);
        String QueryString = queryList.replace("$tablename", tabName);
//        ?????????????????????????????????????????????????????????????????????????
        logger.info("Ok getIDforUndefined: " + QueryString);
        return jdbcTemplate.query(QueryString, new Object[]{}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("id");
            }
            return data;
        });
    }

    public String getTableNameformId(String id) {
        String QueryString = "SELECT PAGES.table FROM PAGES  where PAGES.id = ?  ";
        logger.info(QueryString + " id " + id);
        return jdbcTemplate.query(QueryString, new Object[]{id}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("table");
            }
            return data;
        });
    }

    public String getRefId(String id, String tablename) {
        String QueryString = "SELECT id FROM " + tablename + "  where r_id = ?  ";
        return jdbcTemplate.query(QueryString, new Object[]{id}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("id");
            }
            logger.info("Ok getTableNameformId: " + data);
            return data;
        });
    }

//    public String gettableName(String id) {
//        String queryList = "select PAGES.table from PAGES where id =? ";
//        return jdbcTemplate.query(queryList, new Object[]{id}, (ResultSet rs) -> {
//            String data = "";
//            while (rs.next()) {
//                data = rs.getString("table");
//            }
//            return data;
//        });
//    }
    public String deleteIdlisting(String id, String pageid) {
        String status = null;
        int count = 1;
        logger.info(" inside pageIIIIIIIIID started: " + pageid);
        String queryList1 = null;
        String Tablename = getTableNameformId(pageid);
        logger.info(Tablename);
        queryList1 = "delete from $tablename where r_id =?";
        String recursionqueryLst = queryList1.replace("$tablename", Tablename);
        jdbcTemplate.update(recursionqueryLst, new Object[]{id});
        String nextTable = nextTableID(pageid);
        logger.info(" inside NOEEEEEEEEEEEE nextTablenextTable: " + nextTable);
        String nullHandler = "";
        if (nextTable == null || nextTable == "null" || nextTable.equals(null)) {
            logger.info(" nulHandler started: " + nextTable);
            nullHandler = "nullvalue";
        }
        if (!nullHandler.equals("nullvalue")) {
            count++;
            logger.info("DDDDDDDDDDDDDDDDD started: " + count);
            deleteIdlisting(id, nextTable);
        }
        status = "true";
        return status;

    }

    public String nextTableID(String pageid) {
        logger.info(" recursion  started: " + pageid);
        String nxtid = "SELECT next FROM PAGES where id = ?";
        return jdbcTemplate.query(nxtid, new Object[]{pageid}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("next");
            }
            return data;

        });

// 
//            if (recursionvalue != null) {
//                String queryList1 = "delete from $tablename where r_id =?";
//                String recursiontabname = getTableNameformId(recursionvalue);
//                String recursionqueryLst = queryList1.replace("$tablename", recursiontabname);
//                jdbcTemplate.update(recursionqueryLst, new Object[]{id});
////                recursionDelete(recursionvalue);
    }

    String UpdateData(String tableName, ObjectNode json, String id) {
        String status = "true";
        try {
            String QueryString = "UPDATE " + tableName + " SET JDOC=?  WHERE ID = ?";
            jdbcTemplate.update(QueryString, new Object[]{json.toString(), id});
        } catch (Exception ex) {
            ex.printStackTrace();
            status = "false";
        }
        logger.info("Ok  status for updateData " + status);
        return status;
    }

    String UpdateDataWithRef(String tableName, ObjectNode json, String id) {
        String status = "true";
        try {
            String QueryString = "UPDATE " + tableName + " SET JDOC=?  WHERE r_id = ?";
            jdbcTemplate.update(QueryString, new Object[]{json.toString(), id});
        } catch (Exception ex) {
            ex.printStackTrace();
            status = "false";
        }
        logger.info("Ok  status for updateData " + status);
        return status;
    }

    public String RIdforUpdate(String tablePk, String tableName) {

        String rIdQuery = "SELECT r_id FROM " + tableName + " where id = ?";
        return jdbcTemplate.query(rIdQuery, new Object[]{tablePk}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("r_id");
            }
            return data;
        });
    }

    public String saveMasterDataUtil(JsonNode userData, String master, String id) {
        JSONObject jsondata = new JSONObject();
        String tablename = "MASTER_TYPE";
//        String QueryString = "INSERT INTO " + tablename + " (type) values(?)";
        String QueryString = "INSERT INTO " + tablename + " (type)  VALUES (?)  ON DUPLICATE KEY UPDATE type = ?";
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(QueryString, new String[]{"id"});
                    ps.setString(1, master);
                    ps.setString(2, master);
                    return ps;
                }, keyHolder);
        logger.info(" printing key ==> " + keyHolder.getKey());
        Number Nid = keyHolder.getKey();  // id of that row   
        if (id.equals("0")) {
            id = Nid.toString();
            logger.info(" INSIDE Null key ==> " + id);
        }
        logger.info("Ok  saveMasterDataUtil for id " + id);
        jsondata.put("id", id);
        return id;

    }

    public void saveMasterValueDataUtil(String value, String id) {
        String tableName = "MASTER_VALUE";
        String query = "INSERT INTO $tablename (value,master_type_id) VALUES (?, ?)";
        String QueryString = query.replace("$tablename", tableName);
        try {
            jdbcTemplate.update(QueryString, new Object[]{value, id});
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Ok  saveMasterValueDataUtil for id " + id);
    }

    public String getBycommonQuery(String id) {
        String query = getQuery(id);
        return jdbcTemplate.query(query, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("menu");
            }
            logger.info("getBycommonQuery " + data);
            return data;
        });
    }

    public String getQuery(String id) {
        logger.info(" getQuery " + id);
        String Query = "SELECT QUERIES_COMMON.`query`  FROM  QUERIES_COMMON WHERE id =?";
        return jdbcTemplate.query(Query, new Object[]{id}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("query");
            }
            logger.info("  " + data);
            return data;
        });
    }

    public String gettitleNameBypageId(String pageid) {
        String Query = "SELECT name   FROM  PAGES WHERE id =?";
        return jdbcTemplate.query(Query, new Object[]{pageid}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("name");
            }
            logger.info("Ok  getNameById " + data);
            return data;
        });
    }

//        String query ="SELECT	JSON_OBJECT( 'id', MASTER_TYPE.id, 'name', MASTER_TYPE.type, 'values', GROUP_CONCAT( MASTER_VALUE.`value` ) ) AS jdoc FROM 	`MASTER_VALUE` 	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id`  where MASTER_TYPE.id = ? GROUP BY MASTER_TYPE.type";
//        String query = "SELECT	JSON_OBJECT( 'id', MASTER_TYPE.id, 'name', MASTER_TYPE.type, 'value', \n"
//                + "GROUP_CONCAT( concat('{values:\\'',MASTER_VALUE.`value`, '\\'}' ) ))  AS jdoc \n"
//                + "FROM 	`MASTER_VALUE` 	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id`  where 	MASTER_TYPE.id = ? GROUP BY 	MASTER_TYPE.type\n"
//                + "	 ";
//    String query = "SELECT	JSON_OBJECT(    'data', JSON_OBJECT('value',  JSON_ARRAY(GROUP_CONCAT(   '{details:',  MASTER_VALUE.`value`, '}' ) )   )  ) \n"
//                + " AS jdoc \n"
//                + " FROM\n"
//                + "	`MASTER_VALUE`\n"
//                + "	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id` \n"
//                + " WHERE\n"
//                + "	MASTER_TYPE.id =? \n"
//                + " GROUP BY\n"
//                + "	MASTER_TYPE.type";
    //        String query = "SELECT 	JSON_OBJECT('id',MASTER_TYPE.id,'name',	MASTER_TYPE.type, 'value', concat('[', GROUP_CONCAT( concat  (  '{values:\\'',  MASTER_VALUE.`value`, '\\'}' ) ),']' 	)) AS jdoc FROM	`MASTER_VALUE`	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id` WHERE	MASTER_TYPE.id = ? GROUP BY	MASTER_TYPE.type";
    //        String query = "SELECT	JSON_OBJECT( 'id', MASTER_TYPE.id, 'name', MASTER_TYPE.type, 'value', \n"
//                + "GROUP_CONCAT( concat('{values:\\'',MASTER_VALUE.`value`, '\\'}' ) ))  AS jdoc \n"
//                + "FROM 	`MASTER_VALUE` 	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id`  where 	MASTER_TYPE.id = ? GROUP BY 	MASTER_TYPE.type\n"
//                + "	 ";
//        String query = " SELECT	JSON_OBJECT( 'id', MASTER_TYPE.id, 'name', MASTER_TYPE.type,'value',GROUP_CONCAT( concat('{values:\\'',MASTER_VALUE.`value`, '\\'}' ) ))  AS jdoc \n" +
//"FROM 	`MASTER_VALUE` 	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id`  where 	MASTER_TYPE.id = ? \n" +
//"GROUP BY 	MASTER_TYPE.type";
    public String getMastersValuesById(String id) {
        String query = " SELECT	JSON_OBJECT( 'id', MASTER_TYPE.id, 'name', MASTER_TYPE.type,'value', GROUP_CONCAT( concat('\\{\"values\":\"',MASTER_VALUE.`value`, '\"}' ) ))  AS jdoc \n"
                + " FROM 	`MASTER_VALUE` 	RIGHT JOIN MASTER_TYPE ON MASTER_VALUE.master_type_id = MASTER_TYPE.`id`  where 	MASTER_TYPE.id =  ? \n"
                + " GROUP BY 	MASTER_TYPE.type";
        return jdbcTemplate.query(query, new Object[]{id}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("jdoc");
            }
            logger.info("Ok  getMastersValuesById " + data);
            logger.info("Ok  getMastersValuesById query%%%%%% " + query);
            return data;
        });
    }

    public JSONArray getMasterTypeUtil(String id) {
        JSONArray data = new JSONArray();
        String sql = "SELECT JSON_OBJECT( 'id', MASTER_TYPE.id, 'type',  MASTER_TYPE.`type` ) AS DATA FROM  MASTER_TYPE";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            return data;
        });
    }

    public JSONArray getValueOfMaster(String id) {
        JSONArray data = new JSONArray();
        String sql = "SELECT\n"
                + "	JSON_OBJECT( 'id', id, 'value', `value` ) AS DATA \n"
                + "FROM\n"
                + "	 MASTER_VALUE where master_type_id = ?";
        return jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
            while (rs.next()) {
                data.put(new JSONObject(rs.getString("data")));
            }
            logger.info("Ok  getValueOfMaster ById " + data);
            return data;
        });
    }

    public void M2Mmasterlink(String masterid, String submasterid) {
        String tableName = "MASTER_TYPE";
        String query = "Update $tablename set master_id = ? where id = ?";
        String QueryString = (query.replace("$tablename", tableName));

        jdbcTemplate.update(QueryString, new Object[]{submasterid, masterid});
        logger.info("Ok  M2Mmasterlink query " + query);

    }

    public void M2MmasterValuelink(String mastervalueid, String submastervalues) {
        String tableName = "MASTER_VALUE";
        String query = "Update $tablename set master_value_id = ? where id = ?";
        String QueryString = query.replace("$tablename", tableName);
        jdbcTemplate.update(QueryString, new Object[]{mastervalueid, submastervalues});
        logger.info("Ok  M2Mmasterlink query " + query);
    }

    public String m2mAddForm(String id, String viewpage) {
        logger.info(" INSIDE m2mAddForm started: ");
//     jdbcTemplate.update("UPDATE  MASTER_TYPE SET master_id = null  where id =?", new Object[]{id});
        jdbcTemplate.update("UPDATE  MASTER_VALUE SET master_value_id = null  where master_value_id =?", new Object[]{id});
        return "true";
    }

    public String masterAddForm(String id, String viewpage) {
        logger.info(" INSIDE MASTERTPYE started: ");
        String tableName1 = "MASTER_TYPE";
        String query1 = "delete from $tablename where id =?";
        String QueryString1 = query1.replace("$tablename", tableName1);
        jdbcTemplate.update(QueryString1, new Object[]{id});
        String tableName2 = "MASTER_VALUE";
        String query2 = "delete from $tablename where id =?";
        String QueryString2 = query2.replace("$tablename", tableName2);
        jdbcTemplate.update(QueryString2, new Object[]{id});
        return "true";
    }

    public String getEditM2Mvaluesutil(String id) {
        logger.info(" INSIDE getEditM2Mvaluesutil  id : " + id);
        String Query = "SELECT JSON_OBJECT('mastername', a.VALUE, 'masterid' ,a.id, 'submasternames', GROUP_CONCAT( b.VALUE )) as jdoc FROM MASTER_VALUE a, MASTER_VALUE b WHERE	a.id = b.master_value_id AND a.id =? GROUP BY	a.id";
        return jdbcTemplate.query(Query, new Object[]{id}, (ResultSet rs) -> {
            String data = "";
            while (rs.next()) {
                data = rs.getString("jdoc");
            }
            logger.info("Ok  getNameById " + data);
            return data;
        });
    }

    public void saveTestDataUtil(HttpServletRequest request, String taxperc) {
        // saveTestDataUtil
        String tableName = "T2";
        String id = "184";   // testing
        String query = "UPDATE $tablename SET jdoc = JSON_SET( jdoc, '$.tax', '" + taxperc + "' ) WHERE r_id = ? ";
        String QueryString = query.replace("$tablename", tableName);
        jdbcTemplate.update(QueryString, new Object[]{id});
        logger.info("Ok  saveTestDataUtil " + query);
//        ApplicationLoggingAspect.applicationLog(request, jdbcTemplate, "acad_Course_TimeTable_Details", "pk=34", "Coursepk", "1");

        return;
    }

    public String getPrePostFilter(String saveTestData) {
        String sql = " select prefilter from PAGES where id  = ?";
        return jdbcTemplate.query(sql, new Object[]{saveTestData}, (ResultSet rs) -> {
            String data = null;
            while (rs.next()) {
                data = rs.getString("prefilter");
            }
            logger.info("Ok  getPrePostFilter ById " + data);

            return data;
        });

    }

    public void ApplicationLog() {
        String tableName = "application_log";
        String query = "insert into $tablename   (local_ip, global_ip, schema_name, user_id, module, functionality, operational_pk, action_name, before_values, table_name, date_time, role_name, operation_based_on_multiple_param_status) values( ?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String QueryString = query.replace("$tablename", tableName);
        try {
//            jdbcTemplate.update(query, new Object[]{applicationLoggingModel.getLocalIpAddress(), applicationLoggingModel.getGlobalIpAddress(), applicationLoggingModel.getSchemaName(), applicationLoggingModel.getUserId(), applicationLoggingModel.getModule(), applicationLoggingModel.getFunctionality(), applicationLoggingModel.getOperationalPKValue(), applicationLoggingModel.getOperationName(), applicationLoggingModel.getBeforeActionValues(), applicationLoggingModel.getOperationalTableName(), applicationLoggingModel.getOperationDateTime(), applicationLoggingModel.getUserRole(), applicationLoggingModel.getOperation_based_on_multiple_param_status()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Ok  saveMasterValueDataUtil for ifd ");
    }

    public void saveApplicationLogData() {
        HttpSession session = request.getSession(false);
        String current = session.getAttribute("current").toString();
        String viewpage = session.getAttribute("viewpage").toString();
        String id = session.getAttribute("id").toString();

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Ok  saveMasterValueDataUtil for ifd ");
    }

}

//String query = "insert into application_log     (local_ip, global_ip, schema_name, user_id, module, functionality, operational_pk, action_name, before_values, table_name, date_time, role_name, operation_based_on_multiple_param_status) values( ?,?,?,?,?,?,?,?,?,?,?,?,?)";
//            jdbcTemplate.update(query, new Object[]{applicationLoggingModel.getLocalIpAddress(), applicationLoggingModel.getGlobalIpAddress(), applicationLoggingModel.getSchemaName(), applicationLoggingModel.getUserId(), applicationLoggingModel.getModule(), applicationLoggingModel.getFunctionality(), applicationLoggingModel.getOperationalPKValue(), applicationLoggingModel.getOperationName(), applicationLoggingModel.getBeforeActionValues(), applicationLoggingModel.getOperationalTableName(), applicationLoggingModel.getOperationDateTime(), applicationLoggingModel.getUserRole(), applicationLoggingModel.getOperation_based_on_multiple_param_status()});
//
//select 
//country_master.country_id as master_id,
//country_master.type as master_type ,
//country_master.value as master_value,
//group_concat(state_master.value ) as sub_master_value
//from 
//( SELECT
//MASTER_TYPE.id,
//MASTER_TYPE.type,
//MASTER_VALUE.`value`,
//MASTER_VALUE.id as country_id ,
//MASTER_VALUE.master_type_id
//FROM
//MASTER_TYPE
//INNER JOIN MASTER_VALUE ON MASTER_TYPE.id = MASTER_VALUE.master_type_id
//) as country_master 
//INNER JOIN (
//SELECT MASTER_VALUE.`value`,
//MASTER_VALUE.id  ,
//MASTER_VALUE.master_value_id 
//from MASTER_VALUE
//) as state_master on state_master.master_value_id =country_master.country_id
//group by country_master.country_id 

