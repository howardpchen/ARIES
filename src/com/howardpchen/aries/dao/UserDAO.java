package com.howardpchen.aries.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.howardpchen.aries.model.CaseList;
import com.howardpchen.aries.model.Network;
import com.howardpchen.aries.model.User;
import com.howardpchen.aries.model.UserCaseInput;

public class UserDAO {      
     public static boolean login(String username, String password) {
        Connection con = null;
        PreparedStatement ps = null;
        boolean result = false;
        try {
        	
            con = Database.getConnection();
            ps = con.prepareStatement(
                    "select * from loginuser where username= ? and passhash= ? ");
            ps.setString(1, username);
            ps.setString(2, password);
  
            ResultSet rs = ps.executeQuery();
            if (rs.next()) // found
            { 
            	result = true;
            }
            
        } catch (Exception ex) {
            System.out.println("Error in login() -->" + ex.getMessage());
            
        } finally {
            Database.close(con);
        }
		return result;
    }
     public static boolean checkFeatures(int caseno, String value) {
         Connection con = null;
         PreparedStatement ps = null;
         boolean result = false;
         String Valuefromdb = null;
         try {
         	
             con = Database.getConnection();
             ps = con.prepareStatement(
                     "select value from usercaseinput where caseid= ? and value= ?");
             ps.setInt(1, caseno);
             ps.setString(2, value);
   
             ResultSet rs = ps.executeQuery();
             if (rs.next()) // found
             { 
            		result = true;
             }
             
         } catch (Exception ex) {
             System.out.println("Error in login() -->" + ex.getMessage());
             
         } finally {
             Database.close(con);
         }
 		return result;
     }
     public static int getUserID(String username, String password){
    	 int userid = 0;
    	 Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getConnection();
             ps = con.prepareStatement(
                     "select userid from loginuser where username= ? and passhash=? ");
             ps.setString(1,username);
             ps.setString(2,password);
          
   
             ResultSet rs = ps.executeQuery();
             if (rs.next()) // found
             {
            	 userid = rs.getInt("userid");
             }
             
         } catch (Exception ex) {
             System.out.println("Error in getUserId -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return userid;
     }
     public static String getFileName(String nwName){
    	 Connection con = null;
    	 Statement stmnt = null;
    	 String filename = null;
    	 try{
    		 con = Database.getConnection();
    		 stmnt = con.createStatement();
    		 ResultSet rs=stmnt.executeQuery("select filename from networks where description ='"+nwName+"'");
    		 System.out.println(rs.toString());
    		 while(rs.next()){
    		 filename = rs.getString("filename");
    		 System.out.println("filename :"+filename);}
    	 }catch (Exception ex) {
             System.out.println("Error in getFileName() -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return filename;
    	 
     }
     public static String getCode(String nwName){
    	 Connection con = null;
    	 Statement stmnt = null;
    	 String code = null;
    	 try{
    		 con = Database.getConnection();
    		 stmnt = con.createStatement();
    		 ResultSet rs=stmnt.executeQuery("select code from networks where description ='"+nwName+"'");
    		 while(rs.next()){
    		 code = rs.getString("code");
    		}
    	 }catch (Exception ex) {
             System.out.println("Error in getCode() -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return code;
    	 
     }
     public static String getNwName(String code){
    	 Connection con = null;
    	 Statement stmnt = null;
    	 String nwName = null;
    	 try{
    		 con = Database.getConnection();
    		 stmnt = con.createStatement();
    		 ResultSet rs=stmnt.executeQuery("select description from networks where code ='"+code+"'");
    		 while(rs.next()){
    			 nwName = rs.getString("description");
    		}
    	 }catch (Exception ex) {
             System.out.println("Error in getNwName() -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return nwName;
    	 
     }
     public static String getCodeforFileName(String filename){
    	 Connection con = null;
    	 Statement stmnt = null;
    	 String code = null;
    	 try{
    		 con = Database.getConnection();
    		 stmnt = con.createStatement();
    		 ResultSet rs=stmnt.executeQuery("select code from networks where filename ='"+filename+"'");
    		 while(rs.next()){
    		 code = rs.getString("code");
    		 System.out.println("code :"+ code);}
    	 }catch (Exception ex) {
             System.out.println("Error in getCodeforFileName() -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return code;
    	 
     }
     public static List<UserCaseInput> getUserCaseInput(){
    	 List<UserCaseInput> userCaseInputList = new ArrayList<UserCaseInput>();
    	 Connection con = null;
         Statement  statement = null;
         String query = "select * from usercaseinput";
         UserCaseInput userCaseInput = null;
         try {
             con = Database.getConnection();
             statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query);
             while(rs.next()){
            	 userCaseInput = new UserCaseInput();
            	 userCaseInput.setUserid(rs.getInt("userid"));
            	 userCaseInput.setCaseid(rs.getInt("caseid"));
            	 userCaseInput.setSessionid(rs.getString("sessionid"));
            	 userCaseInput.setEventid(rs.getInt("eventid"));
            	 userCaseInput.setValue(rs.getString("value"));
            	 userCaseInput.setDatetimeentered(rs.getDate("datetimeentered"));
            	 

            	// network.setVersion(rs.getDate("version"));
            	 userCaseInputList.add(userCaseInput);
             }
         } catch (Exception ex) {
             System.out.println("Error in getUserCaseInput() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return userCaseInputList;
    	 
     }
     public static List<UserCaseInput> getUserCaseInput(int caseid){
    	 List<UserCaseInput> userCaseInputList = new ArrayList<UserCaseInput>();
    	 Connection con = null;
         Statement  statement = null;
         String query = "select * from usercaseinput where caseid ='";
         UserCaseInput userCaseInput = null;
         try {
             con = Database.getConnection();
             statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query+caseid+"'");
             while(rs.next()){
            	 userCaseInput = new UserCaseInput();
            	 userCaseInput.setUserid(rs.getInt("userid"));
            	 userCaseInput.setCaseid(rs.getInt("caseid"));
            	 userCaseInput.setSessionid(rs.getString("sessionid"));
            	 userCaseInput.setEventid(rs.getInt("eventid"));
            	 userCaseInput.setValue(rs.getString("value"));
            	 userCaseInput.setDatetimeentered(rs.getDate("datetimeentered"));
            	 

            	// network.setVersion(rs.getDate("version"));
            	 userCaseInputList.add(userCaseInput);
             }
         } catch (Exception ex) {
             System.out.println("Error in getUserCaseInput(int caseid) -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return userCaseInputList;
    	 
     }
     public static List<Network> getNetworkList() {
         Connection con = null;
         Statement  statement = null;
         String query = "select * from networks";
         List<Network> networkList = new ArrayList<Network>();
         Network network = null;
         try {
             con = Database.getConnection();
             statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query);
             while(rs.next()){
            	 network = new Network();
            	 network.setCode(rs.getString("code"));
            	 network.setDescription(rs.getString("description"));
            	 network.setFilename(rs.getString("filename"));
            	// network.setVersion(rs.getDate("version"));
            	 networkList.add(network);
             }
         } catch (Exception ex) {
             System.out.println("Error in getnetworkList() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return networkList;
     }
     public static boolean UpdateCaseList(CaseList caselist){
    	 
    	 boolean updatesuccess = false;
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 Timestamp currentDate = new Timestamp(new Date().getTime());
    	 String sql = "UPDATE caselist SET accession= ?,patientid= ?,network= ?,organization= ?,description= ?,modality= ?,correctdx= ?,age= ?,gender= ?,supportingData= ?,submittedby= ?,submitdate= ?,qualitycontrol = ?,qcperson = ? WHERE caseid= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 
    		 ps.setString(1, caselist.getAccession());
    		 ps.setString(2,caselist.getPatientid());
    		 ps.setString(3, caselist.getNetwork());
    		 ps.setString(4, caselist.getOrganization());
    		 ps.setString(5,caselist.getDescription());
    		 ps.setString(6,caselist.getModality());
    		 ps.setString(7,caselist.getCorrectDx());
    		 ps.setString(8,caselist.getAge());
    		 ps.setString(9,caselist.getGender());
    		 ps.setString(10,caselist.getSupportingData());
    		 ps.setInt(11, caselist.getSubmittedBy());
    		 ps.setTimestamp(12,currentDate);
    		 ps.setString(13, "No");
    		 ps.setString(14, caselist.getQcperson());
    		 ps.setInt(15,caselist.getCaseid());
    		 ps.executeUpdate();
    		 //System.out.println(rs.toString());
    		 updatesuccess=true;
    	 }catch (Exception ex) {
    		 updatesuccess = false;
             System.out.println("Error in Update Case Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return updatesuccess;
     }
 public static boolean QCCaseList(CaseList caselist){
    	 
    	 boolean updatesuccess = false;
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 Timestamp currentDate = new Timestamp(new Date().getTime());
    	 String sql = "UPDATE caselist SET accession= ?,patientid= ?,network= ?,organization= ?,description= ?,modality= ?,correctdx= ?,age= ?,gender= ?,supportingData= ?,submittedby= ?,submitdate= ?,qualitycontrol = ?, qcperson = ? WHERE caseid= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 
    		 ps.setString(1, caselist.getAccession());
    		 ps.setString(2,caselist.getPatientid());
    		 ps.setString(3, caselist.getNetwork());
    		 ps.setString(4, caselist.getOrganization());
    		 ps.setString(5,caselist.getDescription());
    		 ps.setString(6,caselist.getModality());
    		 ps.setString(7,caselist.getCorrectDx());
    		 ps.setString(8,caselist.getAge());
    		 ps.setString(9,caselist.getGender());
    		 ps.setString(10,caselist.getSupportingData());
    		 ps.setInt(11, caselist.getSubmittedBy());
    		 ps.setTimestamp(12,currentDate);
    		 ps.setString(13, "No");
    		 ps.setString(14, caselist.getQcperson());
    		 ps.setInt(15,caselist.getCaseid());
    		 ps.executeUpdate();
    		 //System.out.println(rs.toString());
    		 updatesuccess=true;
    	 }catch (Exception ex) {
    		 updatesuccess = false;
             System.out.println("Error in QC Case Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return updatesuccess;
     }
     public static boolean SaveCaseList(CaseList caselist){
    	 boolean savesuccess = false;
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 Timestamp currentDate = new Timestamp(new Date().getTime());
    	 String sql = "INSERT INTO caselist (accession,patientid,network,organization,description,modality,correctdx,age,gender,supportingData,submittedby,submitdate,qualitycontrol,qcperson)" +
    		        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setString(1, caselist.getAccession());
    		 ps.setString(2,caselist.getPatientid());
    		 ps.setString(3, caselist.getNetwork());
    		 ps.setString(4, caselist.getOrganization());
    		 ps.setString(5,caselist.getDescription());
    		 ps.setString(6,caselist.getModality());
    		 ps.setString(7,caselist.getCorrectDx());
    		 ps.setString(8,caselist.getAge());
    		 ps.setString(9,caselist.getGender());
    		 ps.setString(10,caselist.getSupportingData());
    		 ps.setInt(11, caselist.getSubmittedBy());
    		 ps.setTimestamp(12,currentDate);
    		 ps.setString(13,"No");
    		 ps.setString(14, caselist.getQcperson());
    		 ps.executeUpdate();
    		 //System.out.println(rs.toString());
    		 savesuccess=true;
    	 }catch (Exception ex) {
    		 savesuccess = false;
             System.out.println("Error in Save Case Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
		return savesuccess;
     }
     
     public static void SaveFeature(UserCaseInput userCaseInput){
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 Timestamp currentDate = new Timestamp(new Date().getTime());
    	 String sql = "INSERT INTO usercaseinput (userid,caseid,sessionid,eventid,value,datetimeentered)" +
    		        "VALUES (?, ?, ?, ?, ?, ?)";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setInt(1, userCaseInput.getUserid());
    		 ps.setInt(2, userCaseInput.getCaseid());
    		 ps.setString(3, userCaseInput.getSessionid());
    		 ps.setInt(4, userCaseInput.getEventid());
    		 ps.setString(5,userCaseInput.getValue());
    		 ps.setTimestamp(6,currentDate);
    		 ps.executeUpdate();
    		 //System.out.println(rs.toString());
    		
    	 }catch (Exception ex) {
             System.out.println("Error in Save UserCaseInput(Feature) Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
     }
     public static int getCaseId(String network){
    	 Connection con = null;
    	 Statement stmt = null;
    	 int randomCaseNo = 0;
    	 try{
    		 con = Database.getConnection();
    		 stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT caseid FROM caselist where network = '"+network+"'"+" and qualitycontrol = 'Yes' order by rand() limit 1");
             while(rs.next()){
            	 randomCaseNo = rs.getInt("caseid");
             }
    	 } catch (Exception ex) {
             System.out.println("Error in getRandomCaseId() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return randomCaseNo;
    	
     }
     public static String getAccessionNo(int caseid){
    	 Connection con = null;
    	 Statement stmt = null;
    	 String accession = null;
    	 try{
    		 con = Database.getConnection();
    		 stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT accession FROM caselist where caseid = '"+caseid+"'");
             while(rs.next()){
            	 accession = rs.getString("accession");
             }
    	 } catch (Exception ex) {
             System.out.println("Error in getRandomCaseId() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return accession;
    	
     }
     public static List<String> getCaseIdforQC(String network, String uname){
    	 Connection con = null;
    	 Statement stmt = null;
    	 List<String> caseIdList = new ArrayList<String>();
    	 try{
    		 con = Database.getConnection();
    		 stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT caseid,accession FROM caselist where network = '"+network+"'"+" and qcperson = '"+uname+"'"+"and qualitycontrol= 'No'");
             while(rs.next()){
            	 caseIdList.add(rs.getString("caseid")+ "-" +rs.getString("accession"));
             }
    	 } catch (Exception ex) {
             System.out.println("Error in getCaseIdforQC() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return caseIdList;
    	
     }
     public static CaseList getCaseList(int caseid){
    	 Connection con = null;
         Statement  statement = null;
         String query = ("select * from caselist where caseid = '"+caseid+"'");
        // List<CaseList> caseList = new ArrayList<CaseList>();
         CaseList obj = null;
         try {
             con = Database.getConnection();
             statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query);
             while(rs.next()){
            	 obj = new CaseList();
            	 obj.setCaseid(rs.getInt("caseid"));
            	 obj.setAccession(rs.getString("accession"));
            	 obj.setOrganization(rs.getString("organization"));
            	 obj.setDescription(rs.getString("description"));
            	 obj.setModality(rs.getString("modality"));
            	 obj.setAnatomy(rs.getString("anatomy"));
            	 obj.setCorrectDx(rs.getString("correctdx"));
            	 System.out.println(rs.getString("correctdx"));
            	 obj.setPatientid(rs.getString("patientid"));
            	 obj.setNetwork(rs.getString("network"));
            	 obj.setCompleted(rs.getString("completed"));
            	 obj.setQcperson(rs.getString("qcperson"));
            	 obj.setAge(rs.getString("age"));
            	 obj.setGender(rs.getString("gender"));
            	 System.out.println(rs.getString("supportingData"));
            	 String supportData = rs.getString("supportingData");
            	 String[] supportDataArray = supportData.split(", ");
            			
            	 List<String> newList = new ArrayList(Arrays.asList(supportDataArray));
            	 obj.setSupportingDataList(newList);
            	
             }
         } catch (Exception ex) {
             System.out.println("Error in getCaseList() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return obj;
     }
     
     public static List<CaseList> getCaseList() {
         Connection con = null;
         Statement  statement = null;
         String query = "select * from caselist";
         List<CaseList> caseList = new ArrayList<CaseList>();
         CaseList obj = null;
         try {
             con = Database.getConnection();
             statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query);
             while(rs.next()){
            	 obj = new CaseList();
            	 obj.setCaseid(rs.getInt("caseid"));
            	 obj.setAccession(rs.getString("accession"));
            	 obj.setOrganization(rs.getString("organization"));
            	 obj.setDescription(rs.getString("description"));
            	 obj.setModality(rs.getString("modality"));
            	 obj.setAnatomy(rs.getString("anatomy"));
            	 obj.setCorrectDx(rs.getString("correctdx"));
            	 obj.setPatientid(rs.getString("patientid"));
            	 obj.setNetwork(rs.getString("network"));
            	 obj.setCompleted(rs.getString("completed"));
            	 
            	 caseList.add(obj);
            	
             }
         } catch (Exception ex) {
             System.out.println("Error in getCaseList() -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return caseList;
     }
     public static void UpdateCaseList(String qualitycontrol,int caseid){
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 String sql = "UPDATE caselist set qualitycontrol = ?"+ " WHERE caseid= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setString(1, "Yes");
    		 ps.setInt(2, caseid);
    		
    		
    		 ps.executeUpdate();
    		System.out.println("QC is Completed");
    		
    	 }catch (Exception ex) {
             System.out.println("Error in UpdateCaseList Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
     }
     public static void deleteCaseList(int caseid){
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 String sql = "delete from caselist WHERE caseid= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setInt(1, caseid);
    		 ps.executeUpdate();
    		System.out.println("Case is Deleted");
    		
    	 }catch (Exception ex) {
             System.out.println("Error in Delete Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
     }
     public static void UpdateCaseList(String code,int caseid,String completed){
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 String sql = "UPDATE caselist set completed = ?"+ " WHERE caseid= ?" +" and network= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setString(1, completed);
    		 ps.setInt(2, caseid);
    		 ps.setString(3, code);
    		
    		 ps.executeUpdate();
    		System.out.println("Case is Completed");
    		
    	 }catch (Exception ex) {
             System.out.println("Error in UpdateCaseList Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
     }
     public static void UpdateCaseList(int caseid,String code,String qualitycontrol){
    	 Connection con = null;
    	 PreparedStatement ps = null;
    	 String sql = "UPDATE caselist set qualitycontrol = ?"+ " WHERE caseid= ?" +" and network= ?";
    	 try{
    		 con = Database.getConnection();
    		 ps = con.prepareStatement(sql);
    		 ps.setString(1, qualitycontrol);
    		 ps.setInt(2, caseid);
    		 ps.setString(3, code);
    		
    		 ps.executeUpdate();
    		System.out.println("Case is Completed");
    		
    	 }catch (Exception ex) {
             System.out.println("Error in UpdateCaseList Records -->" + ex.getMessage());
          } finally {
             Database.close(con);
         }
     }
     public static List<String> getUserName() {
    	 Connection con = null;
         Statement stmt = null;
         List<String> userslist = new ArrayList<String>();
         try {
             con = Database.getConnection();
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select username from loginuser");
             while(rs.next()){
            	 userslist.add(rs.getString("username"));
             }
            
         } catch (Exception ex) {
             System.out.println("Error in getUserName()-->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return userslist;
     }
     public static int getCaseId(CaseList caselist) {
    	 int caseid = 0;
    	 Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getConnection();
             ps = con.prepareStatement(
                     "select caseid from caselist where accession= ? and organization= ? and patientid = ? and network = ?");
             ps.setString(1, caselist.getAccession());
             ps.setString(2,caselist.getOrganization());
             ps.setString(3, caselist.getPatientid());
             ps.setString(4, caselist.getNetwork());
          
   
             ResultSet rs = ps.executeQuery();
             if (rs.next()) // found
             {
            	 caseid = rs.getInt("caseid");
             }
             
         } catch (Exception ex) {
             System.out.println("Error in getUserId -->" + ex.getMessage());
         } finally {
             Database.close(con);
         }
		return caseid;
     }
     public static String addLoginDetails(String uname,String password){
    	    int i = 0;  
    	    String success ="false";
            if (uname != null) {  
                PreparedStatement ps = null;  
                Connection con = null;  
                try {  
                     
                        con = Database.getConnection();  
                        if (con != null) {  
                            String sql = "INSERT INTO loginuser(username, passhash) VALUES(?,?)";  
                            ps = con.prepareStatement(sql);  
                            ps.setString(1, uname);  
                            ps.setString(2, password);   
                            i = ps.executeUpdate();  
                            System.out.println("Data Added Successfully");  
                        }  
                     
                } catch (Exception e) {  
                    System.out.println("error in addLoginDetails ---> "+e.getMessage());  
                } finally {  
                    try {  
                        con.close();  
                        ps.close();  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
            if (i > 0) {  
            	success ="true";  
            } else  
            	success ="false";
            return success;
     }
}

