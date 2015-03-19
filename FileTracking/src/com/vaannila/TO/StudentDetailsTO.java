package com.vaannila.TO;

import java.io.Serializable;

public class StudentDetailsTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String processId = "";
	private String fileName;
	private String sourceSystem = "";
	private String erValidation;
	private String overallStatus;
	private String testElementId = "";
	private String studentName = "";
	private String uuid = "";
	private String subtestName = "";
	private String exceptionCode = "";
	private String erSsHistId = "";
	private String barcode = "";
	private String dateScheduled = "";
	private String stateCode = "";
	private String form = "";
	private String erExcdId = "";
	private String testCenterCode = "";
	private String testCenterName = "";
	private String ctbCustomerId = "";
	private String stateName = "";
	private String dob = "";
	private String gender = "";
	private String govermentId = "";
	private String govermentIdType = "";
	private String address = "";
	private String city = "";
	private String county = "";
	private String state = "";
	private String zip = "";
	private String email = "";
	private String alternateEmail = "";
	private String primaryPhoneNumber = "";
	private String cellPhoneNumber = "";
	private String alternatePhoneNumber = "";
	private String resolvedEthnicityRace = "";
	private String homeLanguage = "";
	private String educationLevel = "";
	private String attendCollege = "";
	private String contact = "";
	private String examineeCountyParishCode = "";
	private String registeredOn = "";
	private String registeredTestCenter = "";
	private String registeredTestCenterCode = "";
	private String scheduleId = "";
	private String timeOfDay = "";
	private String checkedInDate = "";
	private String contentTestType = "";
	private String contentTestCode = "";
	private String tascRadiness = "";
	private String ecc = "";
	private String regstTcCountyParishCode = "";
	private String schedTcCountyParishCode = "";
	private String sourceSystemDesc = "";
	private String processedDateFrom = "";
	private String processedDateTo = "";
	private String errorLog = "";
	private String grade;
	private String district;
	private String school;
	private String drcStudentId;
	private String testDate;
	private String contantArea;
	private String errorCode;
	private String log;
	
	public String getTestDate() {
		return testDate;
	}
	public void setTestDate(String testDate) {
		this.testDate = testDate;
	}
	public String getContantArea() {
		return contantArea;
	}
	public void setContantArea(String contantArea) {
		this.contantArea = contantArea;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public String getDrcStudentId() {
		return drcStudentId;
	}
	public void setDrcStudentId(String drcStudentId) {
		this.drcStudentId = drcStudentId;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	public String getErValidation() {
		return erValidation;
	}
	public void setErValidation(String erValidation) {
		this.erValidation = erValidation;
	}
	public String getOverallStatus() {
		return overallStatus;
	}
	public void setOverallStatus(String overallStatus) {
		this.overallStatus = overallStatus;
	}
	public String getTestElementId() {
		return testElementId;
	}
	public void setTestElementId(String testElementId) {
		this.testElementId = testElementId;
	}
	public String getStudentName() {
		return studentName;
	}
	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getSubtestName() {
		return subtestName;
	}
	public void setSubtestName(String subtestName) {
		this.subtestName = subtestName;
	}
	public String getExceptionCode() {
		return exceptionCode;
	}
	public void setExceptionCode(String exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
	public String getErSsHistId() {
		return erSsHistId;
	}
	public void setErSsHistId(String erSsHistId) {
		this.erSsHistId = erSsHistId;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getDateScheduled() {
		return dateScheduled;
	}
	public void setDateScheduled(String dateScheduled) {
		this.dateScheduled = dateScheduled;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public String getForm() {
		return form;
	}
	public void setForm(String form) {
		this.form = form;
	}
	public String getErExcdId() {
		return erExcdId;
	}
	public void setErExcdId(String erExcdId) {
		this.erExcdId = erExcdId;
	}
	public String getTestCenterCode() {
		return testCenterCode;
	}
	public void setTestCenterCode(String testCenterCode) {
		this.testCenterCode = testCenterCode;
	}
	public String getTestCenterName() {
		return testCenterName;
	}
	public void setTestCenterName(String testCenterName) {
		this.testCenterName = testCenterName;
	}
	public String getCtbCustomerId() {
		return ctbCustomerId;
	}
	public void setCtbCustomerId(String ctbCustomerId) {
		this.ctbCustomerId = ctbCustomerId;
	}
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getGovermentId() {
		return govermentId;
	}
	public void setGovermentId(String govermentId) {
		this.govermentId = govermentId;
	}
	public String getGovermentIdType() {
		return govermentIdType;
	}
	public void setGovermentIdType(String govermentIdType) {
		this.govermentIdType = govermentIdType;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAlternateEmail() {
		return alternateEmail;
	}
	public void setAlternateEmail(String alternateEmail) {
		this.alternateEmail = alternateEmail;
	}
	public String getPrimaryPhoneNumber() {
		return primaryPhoneNumber;
	}
	public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
		this.primaryPhoneNumber = primaryPhoneNumber;
	}
	public String getCellPhoneNumber() {
		return cellPhoneNumber;
	}
	public void setCellPhoneNumber(String cellPhoneNumber) {
		this.cellPhoneNumber = cellPhoneNumber;
	}
	public String getAlternatePhoneNumber() {
		return alternatePhoneNumber;
	}
	public void setAlternatePhoneNumber(String alternatePhoneNumber) {
		this.alternatePhoneNumber = alternatePhoneNumber;
	}
	public String getResolvedEthnicityRace() {
		return resolvedEthnicityRace;
	}
	public void setResolvedEthnicityRace(String resolvedEthnicityRace) {
		this.resolvedEthnicityRace = resolvedEthnicityRace;
	}
	public String getHomeLanguage() {
		return homeLanguage;
	}
	public void setHomeLanguage(String homeLanguage) {
		this.homeLanguage = homeLanguage;
	}
	public String getEducationLevel() {
		return educationLevel;
	}
	public void setEducationLevel(String educationLevel) {
		this.educationLevel = educationLevel;
	}
	public String getAttendCollege() {
		return attendCollege;
	}
	public void setAttendCollege(String attendCollege) {
		this.attendCollege = attendCollege;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getExamineeCountyParishCode() {
		return examineeCountyParishCode;
	}
	public void setExamineeCountyParishCode(String examineeCountyParishCode) {
		this.examineeCountyParishCode = examineeCountyParishCode;
	}
	public String getRegisteredOn() {
		return registeredOn;
	}
	public void setRegisteredOn(String registeredOn) {
		this.registeredOn = registeredOn;
	}
	public String getRegisteredTestCenter() {
		return registeredTestCenter;
	}
	public void setRegisteredTestCenter(String registeredTestCenter) {
		this.registeredTestCenter = registeredTestCenter;
	}
	public String getRegisteredTestCenterCode() {
		return registeredTestCenterCode;
	}
	public void setRegisteredTestCenterCode(String registeredTestCenterCode) {
		this.registeredTestCenterCode = registeredTestCenterCode;
	}
	public String getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}
	public String getTimeOfDay() {
		return timeOfDay;
	}
	public void setTimeOfDay(String timeOfDay) {
		this.timeOfDay = timeOfDay;
	}
	public String getCheckedInDate() {
		return checkedInDate;
	}
	public void setCheckedInDate(String checkedInDate) {
		this.checkedInDate = checkedInDate;
	}
	public String getContentTestType() {
		return contentTestType;
	}
	public void setContentTestType(String contentTestType) {
		this.contentTestType = contentTestType;
	}
	public String getContentTestCode() {
		return contentTestCode;
	}
	public void setContentTestCode(String contentTestCode) {
		this.contentTestCode = contentTestCode;
	}
	public String getTascRadiness() {
		return tascRadiness;
	}
	public void setTascRadiness(String tascRadiness) {
		this.tascRadiness = tascRadiness;
	}
	public String getEcc() {
		return ecc;
	}
	public void setEcc(String ecc) {
		this.ecc = ecc;
	}
	public String getRegstTcCountyParishCode() {
		return regstTcCountyParishCode;
	}
	public void setRegstTcCountyParishCode(String regstTcCountyParishCode) {
		this.regstTcCountyParishCode = regstTcCountyParishCode;
	}
	public String getSchedTcCountyParishCode() {
		return schedTcCountyParishCode;
	}
	public void setSchedTcCountyParishCode(String schedTcCountyParishCode) {
		this.schedTcCountyParishCode = schedTcCountyParishCode;
	}
	
	public String getSourceSystemDesc() {
		return sourceSystemDesc;
	}
	public void setSourceSystemDesc(String sourceSystemDesc) {
		this.sourceSystemDesc = sourceSystemDesc;
	}
	public String getProcessedDateFrom() {
		return processedDateFrom;
	}
	public void setProcessedDateFrom(String processedDateFrom) {
		this.processedDateFrom = processedDateFrom;
	}
	public String getProcessedDateTo() {
		return processedDateTo;
	}
	public void setProcessedDateTo(String processedDateTo) {
		this.processedDateTo = processedDateTo;
	}
	public String getErrorLog() {
		return errorLog;
	}
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		if("ERESOURCE".equals(sourceSystem)){
			buffer.append("\"").append(erSsHistId).append("\"");
		}else{
			buffer.append("\"").append(processId).append("\"");
		}
		buffer.append(",").append("\"").append(studentName).append("\"")
		.append(",").append("\"").append(uuid).append("\"")
		.append(",").append("\"").append(testElementId).append("\"")
		.append(",").append("\"").append(processId).append("\"")
		.append(",").append("\"").append(exceptionCode).append("\"");
		if("CO".equals(overallStatus)) {
			buffer.append(",").append("\"").append("Completed").append("\"");
		}else if("ER".equals(overallStatus)) {
			buffer.append(",").append("\"").append("Error").append("\"");
		}else if("IN".equals(overallStatus)) {
			buffer.append(",").append("\"").append("Invalidated").append("\"");
		}else{
			buffer.append(",").append("\"").append("In Progress").append("\"");
		}
		buffer.append(",").append("\"").append(barcode).append("\"")
		.append(",").append("\"").append(dateScheduled).append("\"")
		.append(",").append("\"").append(stateCode).append("\"")
		.append(",").append("\"").append(form).append("\"")
		.append(",").append("\"").append(subtestName).append("\"")
		.append(",").append("\"").append(testCenterCode).append("\"")
		.append(",").append("\"").append(testCenterName).append("\"");
		if("ERESOURCE".equals(sourceSystem)){
			buffer.append(",").append("\"").append(ctbCustomerId).append("\"")
			.append(",").append("\"").append(stateName).append("\"")
			.append(",").append("\"").append(dob).append("\"")
			.append(",").append("\"").append(gender).append("\"")
			.append(",").append("\"").append(govermentId).append("\"")
			.append(",").append("\"").append(govermentIdType).append("\"")
			.append(",").append("\"").append(address).append("\"")
			.append(",").append("\"").append(city).append("\"")
			.append(",").append("\"").append(county).append("\"")
			.append(",").append("\"").append(state).append("\"")
			.append(",").append("\"").append(zip).append("\"")
			.append(",").append("\"").append(email).append("\"")
			.append(",").append("\"").append(alternateEmail).append("\"")
			.append(",").append("\"").append(primaryPhoneNumber).append("\"")
			.append(",").append("\"").append(cellPhoneNumber).append("\"")
			.append(",").append("\"").append(alternatePhoneNumber).append("\"")
			.append(",").append("\"").append(resolvedEthnicityRace).append("\"")
			.append(",").append("\"").append(homeLanguage).append("\"")
			.append(",").append("\"").append(educationLevel).append("\"")
			.append(",").append("\"").append(attendCollege).append("\"")
			.append(",").append("\"").append(contact).append("\"")
			.append(",").append("\"").append(examineeCountyParishCode).append("\"")
			.append(",").append("\"").append(registeredOn).append("\"")
			.append(",").append("\"").append(registeredTestCenter).append("\"")
			.append(",").append("\"").append(registeredTestCenterCode).append("\"")
			.append(",").append("\"").append(scheduleId).append("\"")
			.append(",").append("\"").append(timeOfDay).append("\"")
			.append(",").append("\"").append(checkedInDate).append("\"")
			.append(",").append("\"").append(contentTestType).append("\"")
			.append(",").append("\"").append(contentTestCode).append("\"")
			.append(",").append("\"").append(tascRadiness).append("\"")
			.append(",").append("\"").append(ecc).append("\"")
			.append(",").append("\"").append(regstTcCountyParishCode).append("\"")
			.append(",").append("\"").append(schedTcCountyParishCode).append("\"");
		}
		buffer.append(",").append("\"").append(sourceSystemDesc).append("\"")
		.append(",").append("\"").append(processedDateFrom).append("\"")
		.append(",").append("\"").append(processedDateTo).append("\"")
		.append(",").append("\"").append(errorLog).append("\"");
		return buffer.toString();
	}
	
	
}