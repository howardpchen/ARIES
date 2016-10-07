package com.howardpchen.aries.dao;

import com.howardpchen.aries.Util;
import com.howardpchen.aries.dao.UserDAO;
import com.howardpchen.aries.model.UserInfo;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;



 
@ManagedBean
@SessionScoped
/**
 *
 * @author User
 */
public class LoginBean implements Serializable {
 
    private static final long serialVersionUID = 1L;
    private String password;
    private String message, uname;
	private String option;
	private String firstname;
	private String lastname;
	private String organization;
	private String traininglevel;
	private String email;
	private String orgText = "Please supply organization name";
	
	private boolean knownOrg = true;
	
	public boolean isKnownOrg() {
		System.out.println("isKnownOrg()");
		return knownOrg;
	}
	
	public boolean getKnownOrg() {
		System.out.println("getKnownOrg()");
		return knownOrg;
	}
	
	public void setKnownOrg(boolean isKnown) {
		System.out.println("setKnownOrg()");
		knownOrg = isKnown;
	}
	
	
	

	public String getOrgText() {
		return orgText;
	}

	public void setOrgText(String orgText) {
		this.orgText = orgText;
		System.out.println("setOrgText(" + orgText + ")" );
	}
	public LoginBean(){
    	this.setOption("Clinical");
    }
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public String getUname() {
        return uname;
    }
 
    public void setUname(String uname) {
        this.uname = uname;
    }
    public String register() {
    	boolean result = UserDAO.login(uname, password);
    	if(result == true){
    		   FacesContext.getCurrentInstance().addMessage(
                       null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Username and Password combination is already there",
                       "Please Try Again!"));
    		   return null;
    	}
    	UserInfo userinfo = new UserInfo();
    	userinfo.setFirstname(this.getFirstname());
    	userinfo.setLastname(this.getLastname());
    	userinfo.setOrganization(this.getOrganization());
    	userinfo.setTraininglevel(this.getTraininglevel());
    	userinfo.setEmail(this.getEmail());
    	userinfo.setUname(this.getUname());
    	userinfo.setPassword(this.getPassword());
    	String success= UserDAO.addLoginDetails(userinfo);
    	if(success.equalsIgnoreCase("true")){
    		FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registration Successful",
                    "Please Try Again!"));
    	/*	this.setUname("");
    		this.setPassword("");
    		this.setFirstname("");
    		this.setLastname("");
    		this.setOrganization("");
    		this.setTraininglevel("");
    		this.setEmail("");*/
    		//new LoginAction().sendHTMLEmail();   	
    		}
    	else{
    		 FacesContext.getCurrentInstance().addMessage(
                     null,
                     new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Registration failed.Please check in console",
                     "Please Try Again!"));
    	}
    	return "";
    }
   
    public String loginProject() {
        boolean result = UserDAO.login(uname, password);
      
        if (result) {
        	  
            // get Http Session and store username
            HttpSession session = Util.getSession();
            session.setAttribute("username", uname);
            session.setAttribute("password", password);
           // int userid = UserDAO.getUserID(uname,password);
           // session.setAttribute("User", obj);
           // this.setUname("");
           // this.setFullname("");
            System.out.println("this.getOption() .."+this.getOption());
            if("QC".equals(this.getOption())&& (!uname.equalsIgnoreCase("manuel")|| !uname.equalsIgnoreCase("manuel1") || !uname.equalsIgnoreCase("manuel2") || uname.equalsIgnoreCase("manuel3")
            		|| !uname.equalsIgnoreCase("manuel4") || !uname.equalsIgnoreCase("siva")|| !uname.equalsIgnoreCase("yamuna"))){
            	 FacesContext.getCurrentInstance().addMessage(
                         null,
                         new FacesMessage(FacesMessage.SEVERITY_ERROR,
                         "Access to QC is restricted to selected users only",
                         ""));
            	
            }
            if("Clinical".equals(this.getOption())){
                return "index?faces-redirect=true";
            }
            else if("Submit Case".equals(this.getOption())){
            	return "caseInput_form?faces-redirect=true";
            }
            else if("Research".equals(this.getOption())){
            	return "research?faces-redirect=true";
            }
            else if("Education".equals(this.getOption())){
            	return "education?faces-redirect=true";
            }
            else if("QC".equals(this.getOption())&& (uname.equalsIgnoreCase("manuel")|| uname.equalsIgnoreCase("manuel1") || uname.equalsIgnoreCase("manuel2") || uname.equalsIgnoreCase("manuel3")
            		|| uname.equalsIgnoreCase("manuel4") )){
            	return "qualityControl?faces-redirect=true";
            }
           
        } else {
        	
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Username or Password is incorrect",
                    "Please Try Again!"));
        }
        return ""; 
    }
 
    public String logout() {
      HttpSession session = Util.getSession();
      session.invalidate();
      return "login";
   }

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		System.out.println("setOrganization("+organization+")");
		this.organization = organization;
		
		if (organization.equals("Other")) {
			setKnownOrg(false);
			setOrgText("Please supply organization name");
		}
		else {
			setKnownOrg(true);
			setOrgText("");
		}
	}
	
	public String getTraininglevel() {
		return traininglevel;
	}
	
	public void setTraininglevel(String traininglevel) {
		this.traininglevel = traininglevel;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	
}
