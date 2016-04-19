package com.howardpchen.aries.model;

import java.util.Date;
import java.util.List;

public class CaseList {

	private int caseid;
	private String accession;
	private String organization;
	private String description;
	private String modality;
	private String anatomy;
	private String correctDx;
	private String patientid;
	private String network;
	private int submittedBy;
	private Date SubmittedDate;
	private String age;
	private String supportingData;
	private String completed;
	private String qcperson;
	private List<String> supportingDataList;
	
	public List<String> getSupportingDataList() {
		return supportingDataList;
	}

	public void setSupportingDataList(List<String> supportingDataList) {
		this.supportingDataList = supportingDataList;
	}

	public String getCompleted() {
		return completed;
	}

	public void setCompleted(String completed) {
		this.completed = completed;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	private String gender;

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public int getCaseid() {
		return caseid;
	}

	public void setCaseid(int caseid) {
		this.caseid = caseid;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getAnatomy() {
		return anatomy;
	}

	public void setAnatomy(String anatomy) {
		this.anatomy = anatomy;
	}

	public String getCorrectDx() {
		return correctDx;
	}

	public void setCorrectDx(String correctDx) {
		this.correctDx = correctDx;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	/*public String getSubmittedBy() {
		return submittedBy;
	}

	public void setSubmittedBy(String submittedBy) {
		this.submittedBy = submittedBy;
	}*/

	public Date getSubmittedDate() {
		return SubmittedDate;
	}

	public void setSubmittedDate(Date submittedDate) {
		SubmittedDate = submittedDate;
	}

	public String getPatientid() {
		return patientid;
	}

	public void setPatientid(String patientid) {
		this.patientid = patientid;
	}

	public String getSupportingData() {
		return supportingData;
	}

	public void setSupportingData(String supportingData) {
		this.supportingData = supportingData;
	}

	public int getSubmittedBy() {
		return submittedBy;
	}

	public void setSubmittedBy(int submittedBy) {
		this.submittedBy = submittedBy;
	}

	public String getQcperson() {
		return qcperson;
	}

	public void setQcperson(String qcperson) {
		this.qcperson = qcperson;
	}


}
