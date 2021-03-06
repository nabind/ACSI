package com.ctb.prism.admin.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ctb.prism.admin.transferobject.ObjectValueTO;
import com.ctb.prism.admin.transferobject.OrgTO;
import com.ctb.prism.admin.transferobject.OrgTreeTO;
import com.ctb.prism.admin.transferobject.RoleTO;
import com.ctb.prism.admin.transferobject.UserTO;
import com.ctb.prism.core.constant.IApplicationConstants;
import com.ctb.prism.core.constant.IQueryConstants;
import com.ctb.prism.core.dao.BaseDAO;
import com.ctb.prism.core.exception.BusinessException;
import com.ctb.prism.core.logger.IAppLogger;
import com.ctb.prism.core.logger.LogFactory;
import com.ctb.prism.core.util.CustomStringUtil;
import com.ctb.prism.core.util.LdapManager;
import com.ctb.prism.core.util.PasswordGenerator;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.TriggersRemove;

@Repository("adminDAO")
public class AdminDAOImpl extends BaseDAO implements IAdminDAO {

	@Autowired
	private LdapManager ldapManager;
	
	private static final IAppLogger logger = LogFactory.getLoggerInstance(AdminDAOImpl.class.getName());
	
	/**
	 * Returns the organizationList on load.
	 * 
	 * @param nodeid
	 * @return
	 */
	public ArrayList<OrgTO> getOrganizationDetailsOnFirstLoad(String nodeid) {

		ArrayList<OrgTO> OrgTOs = new ArrayList<OrgTO>();
		List<Map<String, Object>> lstData = getJdbcTemplatePrism()
				.queryForList(IQueryConstants.GET_CURR_TENANT_DETAILS, nodeid);
		if (lstData.size() > 0) {
			OrgTOs = new ArrayList<OrgTO>();
			for (Map<String, Object> fieldDetails : lstData) {
				OrgTO to = new OrgTO();
				to.setTenantId(((BigDecimal) fieldDetails.get("ID"))
						.longValue());
				to.setTenantName((fieldDetails.get("ORG_NAME")).toString());
				to.setParentTenantId(((BigDecimal) fieldDetails.get("PARENTID"))
						.longValue());
				to.setOrgLevel(((BigDecimal) fieldDetails.get("ORG_LEVEL"))
						.longValue());
				OrgTOs.add(to);
			}
		}

		return OrgTOs;
	}
	/**
	 * Returns the organizationList on load.
	 * 
	 * @param nodeid
	 * @return
	 */
	public ArrayList<OrgTO> getOrganizationDetailsOnClick(String nodeid) {

		ArrayList<OrgTO> OrgTOs = new ArrayList<OrgTO>();
		List<Map<String, Object>> lstData = getJdbcTemplatePrism()
				.queryForList(IQueryConstants.GET_TENANT_DETAILS, nodeid);
		if (lstData.size() > 0) {
			OrgTOs = new ArrayList<OrgTO>();
			for (Map<String, Object> fieldDetails : lstData) {
				OrgTO to = new OrgTO();
				to.setTenantId(((BigDecimal) fieldDetails.get("ID"))
						.longValue());
				to.setTenantName((fieldDetails.get("ORG_NAME")).toString());
				to.setParentTenantId(((BigDecimal) fieldDetails.get("PARENTID"))
						.longValue());
				to.setOrgLevel(((BigDecimal) fieldDetails.get("ORG_LEVEL"))
						.longValue());
				OrgTOs.add(to);
			}
		}

		return OrgTOs;
	}
	
	
	/**
	 * Returns the organizationList to create a tree structure.
	 * 
	 * @param nodeid
	 * @return
	 */
	@Cacheable(cacheName = "orgTreeChildren")
	public ArrayList<OrgTreeTO> getOrganizationTree(String nodeid,String currOrg,boolean isFirstLoad, String adminYear)throws Exception 
	{
		ArrayList<OrgTreeTO> OrgTreeTOs = new ArrayList<OrgTreeTO>();
		List<Map<String, Object>> lstData=null;
		if (isFirstLoad)
		{
			lstData= getJdbcTemplatePrism()
			.queryForList(IQueryConstants.GET_CURR_TENANT_DETAILS, nodeid, adminYear);
		}
		else
		{	if(nodeid.indexOf("_") > 0){
				String orgParentId=nodeid.substring(0, nodeid.indexOf("_"));
				String orgLevel=nodeid.substring((nodeid.indexOf("_") + 1),
										nodeid.length());
				if(!("1".equals(orgLevel))){
					lstData = getJdbcTemplatePrism().queryForList(IQueryConstants.GET_TENANT_DETAILS_NON_ACSI,orgParentId,currOrg,adminYear);
					logger.log(IAppLogger.DEBUG, "Tree for non ACSI Users...Currorg="+currOrg);
				}else{
					lstData = getJdbcTemplatePrism()
					.queryForList(IQueryConstants.GET_TENANT_DETAILS, orgParentId, adminYear);
				}
				
			}else{
				lstData = getJdbcTemplatePrism()
				.queryForList(IQueryConstants.GET_TENANT_DETAILS, nodeid, adminYear);
			}
			
		}
		
		if (lstData.size() > 0) {
		
			for (Map<String, Object> fieldDetails : lstData) {
				OrgTO to = new OrgTO();
				OrgTreeTO treeTo = new OrgTreeTO();
							
				to.setId(((BigDecimal) fieldDetails.get("ID"))
						.longValue());
				to.setParentTenantId(((BigDecimal) fieldDetails.get("PARENTID"))
						.longValue());
				to.setOrgLevel(((BigDecimal) fieldDetails.get("ORG_LEVEL"))
						.longValue());
				
				treeTo.setState("closed");
				treeTo.setOrgTreeId(((BigDecimal) fieldDetails.get("ID"))
						.longValue());
				treeTo.setData((String)(fieldDetails.get("ORG_NAME")));
				treeTo.setMetadata(to);
				treeTo.setAttr(to);
				OrgTreeTOs.add(treeTo);
			}
		}
		
		return OrgTreeTOs;
	}
	
	/**
	 * Returns the organizationList to create a tree structure on 
	 * redirecting from manage organozations page 
	 * while clicked on the number of usres .
	 * 
	 * 
	 * @param nodeid
	 * @return
	 */
	//@Cacheable(cacheName = "orgTreeChildren")
	public String getOrganizationTreeOnRedirect(String selectedOrgId,String parentOrgId,String userId,String userName, boolean isRedirected)throws Exception 
	{
		StringBuffer hierarcialOrgIds=new StringBuffer();
		String cummsSeperatedId = "";
		List<Map<String, Object>> hierarcialOrgIdList=null;
		if (isRedirected) {
			hierarcialOrgIdList = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_ORG_HIERARCHY_ON_REDIRECT,
					selectedOrgId, parentOrgId, userId, userName);
		}
	
		if ((hierarcialOrgIdList != null) && (hierarcialOrgIdList.size() > 0)) {
			for (Map<String, Object> orgId : hierarcialOrgIdList) {
				String hierarcialOrgId = (String) orgId.get("ORG_ID");
				hierarcialOrgIds.append(hierarcialOrgId).append(",");

			}
			cummsSeperatedId = hierarcialOrgIds.toString();
			cummsSeperatedId = cummsSeperatedId.substring(0,
					hierarcialOrgIds.length() - 1);
		}
		return cummsSeperatedId;
	}
		
		
	
	
	
	
	
	/**
	 * Returns the organizationList to create a tree structure.
	 * for manage organizations
	 * @param nodeid
	 * @return
	 */
	public ArrayList<OrgTreeTO> getOrgTree(String nodeid,boolean isFirstLoad, String adminYear)throws Exception 
	{
		ArrayList<OrgTreeTO> OrgTreeTOs = new ArrayList<OrgTreeTO>();
		List<Map<String, Object>> lstData;
		if (isFirstLoad)
		{
			lstData= getJdbcTemplatePrism()
			.queryForList(IQueryConstants.GET_ORGANIZATION_LIST, nodeid, nodeid, adminYear, adminYear, nodeid, nodeid);
		}
		else
		{
			lstData = getJdbcTemplatePrism()
			.queryForList(IQueryConstants.GET_ORG_CHILDREN_LIST, adminYear, adminYear, nodeid, adminYear);
		}
		
		if (lstData.size() > 0) {
		
			for (Map<String, Object> fieldDetails : lstData) {
				OrgTO to = new OrgTO();
				OrgTreeTO treeTo = new OrgTreeTO();
							
				to.setId(((BigDecimal) fieldDetails.get("ORG_ID"))
						.longValue());
				to.setParentTenantId(((BigDecimal) fieldDetails.get("ORG_PARENT_ID"))
						.longValue());
				to.setOrgLevel(((BigDecimal) fieldDetails.get("ORG_LEVEL"))
						.longValue());
			
				
				treeTo.setState("closed");
				treeTo.setOrgTreeId(((BigDecimal) fieldDetails.get("ORG_ID"))
						.longValue());
				treeTo.setData((String)(fieldDetails.get("ORG_NAME")));
				treeTo.setMetadata(to);
				treeTo.setAttr(to);
				OrgTreeTOs.add(treeTo);
			}
		}
		
		return OrgTreeTOs;
	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * Returns the userList on load.
	 * 
	 * @param nodeid
	 * @return
	 */
	@Cacheable(cacheName="orgUsers")
	public ArrayList<UserTO> getUserDetailsOnClick(String nodeId,String currorg, String adminYear, String searchParam) {

		ArrayList<UserTO> UserTOs = new ArrayList<UserTO>();
		ArrayList<RoleTO> RoleTOs = new ArrayList<RoleTO>();
		String userName = "";
		String tenantId = "";
		List<Map<String, Object>> lstData = null;
		if (nodeId.indexOf("_") > 0) {
			userName = nodeId.substring((nodeId.indexOf("_") + 1),
					nodeId.length());
			tenantId = nodeId.substring(0, nodeId.indexOf("_"));
			logger.log(IAppLogger.DEBUG, "userName is ................."+ userName);
			if(searchParam != null && searchParam.trim().length() > 0) {
				searchParam = CustomStringUtil.appendString("%", searchParam, "%");
				lstData = getJdbcTemplatePrism().queryForList(
						IQueryConstants.GET_USER_DETAILS_ON_SCROLL_WITH_SRCH_PARAM,adminYear, tenantId, userName, adminYear, searchParam, searchParam, searchParam);
			} else {
				lstData = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_USER_DETAILS_ON_SCROLL,adminYear, tenantId, userName, adminYear);
			}
		} else {
			tenantId = nodeId;
			lstData = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_USER_DETAILS_ON_FIRST_LOAD,adminYear, tenantId, adminYear);
			logger.log(IAppLogger.DEBUG, lstData.size()+"");
		}

		if (lstData.size() > 0) {
			UserTOs = new ArrayList<UserTO>();
			for (Map<String, Object> fieldDetails : lstData) {
				UserTO to = new UserTO();
				long userId=((BigDecimal) fieldDetails.get("USER_ID"))
				.longValue();
				to.setUserId(userId);
				//fetching role for each users
				if( (String.valueOf(userId) != null)&& ((String) (fieldDetails.get("USERNAME")) != null)) 
				{
					List<Map<String, Object>> roleList = null;
					roleList= getJdbcTemplatePrism().queryForList(
							IQueryConstants.GET_USER_ROLE, userId,IApplicationConstants.DEFAULT_USER_ROLE);
					if (roleList.size() > 0)
					{
						RoleTOs = new ArrayList<RoleTO>();
						for (Map<String, Object> roleDetails : roleList){
							
							RoleTO rleTo= new RoleTO();
							rleTo.setRoleId(((BigDecimal) roleDetails.get("ROLE_ID"))
									.longValue());
							rleTo.setRoleName((String) (roleDetails.get("ROLE_NAME")));
							
							RoleTOs.add(rleTo);
						}
						to.setAvailableRoleList(RoleTOs);
					}
									
				}
				to.setUserName((String) (fieldDetails.get("USERNAME")));
					if((String) (fieldDetails.get("FULLNAME"))!=null)
					{
						to.setUserDisplayName((String) (fieldDetails.get("FULLNAME")));
					}
					else
					{
						to.setUserDisplayName("");
					}
				
				to.setStatus((String) (fieldDetails.get("STATUS")));
				to.setTenantId(((BigDecimal) fieldDetails.get("ORG_ID"))
						.longValue());
				to.setParentId(((BigDecimal) fieldDetails.get("ORG_PARENT_ID"))
						.longValue());
				try {
					to.setLoggedInOrgId(Long.parseLong(currorg));
				} catch (NumberFormatException e) {}
				to.setTenantName((String) (fieldDetails.get("ORG_NAME")));
				to.setUserType((String) (fieldDetails.get("USER_TYPE")));
				UserTOs.add(to);
			}
		}

		return UserTOs;
	}

	/**
	 * Returns the userTO on Edit.
	 * 
	 * @param nodeid
	 * @return
	 */
	public UserTO getEditUserData(String nodeid) {

		UserTO to = new UserTO();
		List<RoleTO> availableRoleList = new ArrayList<RoleTO>();
		RoleTO roleTO = null;
		List<RoleTO> masterRoleList = getMasterRoleList("user", nodeid);
		List<Map<String, Object>> lstData = getJdbcTemplatePrism()
				.queryForList(IQueryConstants.GET_USER_DETAILS_ON_EDIT, nodeid);
		List<Map<String, Object>> lstRoleData = getJdbcTemplatePrism()
				.queryForList(IQueryConstants.GET_USER_ROLE_ON_EDIT, nodeid);
		for (Map<String, Object> fieldDetails : lstRoleData) {
			roleTO = new RoleTO();
			String roleName=((String) (fieldDetails.get("ROLENAME")));
			if (!(IApplicationConstants.DEFAULT_USER_ROLE.equals(roleName))){
				roleTO.setRoleName(roleName);
				availableRoleList.add(roleTO);
			}			
		}
		to.setAvailableRoleList(availableRoleList);
		to.setMasterRoleList(masterRoleList);
		for (Map<String, Object> fieldDetails : lstData) {
			to.setUserId(((BigDecimal) fieldDetails.get("ID")).longValue());
			to.setUserName((String) (fieldDetails.get("USERID")));
			to.setUserDisplayName((String) (fieldDetails.get("USERNAME")));
			to.setEmailId((String) (fieldDetails.get("EMAIL")));
			to.setStatus((String) (fieldDetails.get("STATUS")));
		}
		return to;
	}

	/**
	 * Returns the RoleTO on Add.
	 * 
	 * @param userId
	 * @return
	 */
	public List<RoleTO> getRoleOnAddUser(String orgLevel) {
		return getMasterRoleList("org", orgLevel);
	}

	/**
	 * Returns the masterRoleTO on Edit/Add.
	 * 
	 * @param argType
	 *            ,userId
	 * @return
	 */
	private List<RoleTO> getMasterRoleList(String argType, String userid) {
		RoleTO masterRoleTO = null;
		List<RoleTO> masterRoleList = new ArrayList<RoleTO>();
		List<Map<String, Object>> lstMasterRoleData = null;
		long user_org_level = -1;
		if ("user".equals(argType)) {
			user_org_level = getJdbcTemplatePrism().queryForLong(
					IQueryConstants.GET_USER_ORG_LEVEL, userid);
			logger.log(IAppLogger.DEBUG, "user_org_level" + user_org_level);
		} else if ("org".equals(argType)) {
			user_org_level = Long.valueOf(userid);
		}
		if (user_org_level == 1) {
			lstMasterRoleData = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_ROLE_ACSI);
		} else if (user_org_level == 3) {
			lstMasterRoleData = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_ROLE_SCHOOL);
		} else if (user_org_level == 4) {
			lstMasterRoleData = getJdbcTemplatePrism().queryForList(
					IQueryConstants.GET_ROLE_TEACHER);
		}
		for (Map<String, Object> fieldDetails : lstMasterRoleData) {
			masterRoleTO = new RoleTO();
			String roleName = (String) (fieldDetails.get("ROLE_NAME"));
			masterRoleTO.setRoleName((String) (fieldDetails.get("ROLE_NAME")));
			masterRoleTO.setRoleId(((BigDecimal) fieldDetails.get("ROLE_ID"))
					.longValue());
			
			if (user_org_level == 1) {
				if (IApplicationConstants.ROLE_TYPE.ROLE_ACSI.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else if (IApplicationConstants.ROLE_TYPE.ROLE_USER.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else {
					masterRoleTO.setDefaultSelection("");
				}
			} else if (user_org_level == 3) {
				if (IApplicationConstants.ROLE_TYPE.ROLE_SCHOOL.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else if (IApplicationConstants.ROLE_TYPE.ROLE_USER.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else {
					masterRoleTO.setDefaultSelection("");
				}
			} else if (user_org_level == 4) {
				if (IApplicationConstants.ROLE_TYPE.ROLE_CLASS.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else if (IApplicationConstants.ROLE_TYPE.ROLE_USER.toString().equals(roleName)) {
					masterRoleTO.setDefaultSelection("selected");
				} else {
					masterRoleTO.setDefaultSelection("");
				}
			}
			masterRoleList.add(masterRoleTO);
		}
		return masterRoleList;
	}

	/**
	 * Returns boolean.
	 * 
	 * @param Id
	 *            userId, userName, emailId, password, userStatus,userRoles
	 * @return
	 */
	@TriggersRemove(cacheName="orgUsers", removeAll=true)
	public boolean updateUser(String Id, String userId, String userName,
			String emailId, String password, String userStatus,
			String[] userRoles) throws BusinessException, Exception {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - updateUser");
		try {
			boolean ldapFlag = true;
			if (password != null && !"".equals(password)) {
				ldapFlag = 	ldapManager.updateUser(userId, userId, userId, password);		
			}
			if (ldapFlag) {
				// update user table
				getJdbcTemplatePrism().update(IQueryConstants.UPDATE_USER,
						userId, userName, emailId, userStatus, Id);
				// delete from userRole table
				getJdbcTemplatePrism().update(IQueryConstants.DELETE_USER_ROLE,
						Id);

				if (userRoles != null) {
					logger.log(IAppLogger.DEBUG, IApplicationConstants.DEFAULT_USER_ROLE);
					getJdbcTemplatePrism().update(IQueryConstants.INSERT_USER_ROLE, IApplicationConstants.DEFAULT_USER_ROLE, Id);
					for (String role : userRoles) {
						logger.log(IAppLogger.DEBUG, role);
						getJdbcTemplatePrism().update(IQueryConstants.INSERT_USER_ROLE, role, Id);
					}
				}
			} else {
				return false;
			}

		} catch (BusinessException bex) {
			throw new BusinessException(bex.getCustomExceptionMessage());
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while updating user details.", e);
			return false;
		}
		return true;		
	}

	/**
	 * Returns boolean.
	 * 
	 * @param Id
	 *            userId, userName, password
	 * @return
	 */

	@TriggersRemove(cacheName="orgUsers", removeAll=true)
	public boolean deleteUser(String Id, String userName, String password)
			throws Exception {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - deleteUser");
		try {
			//if (ldapManager.deleteUser(userName, userName, userName)) {
				// delete the security answers from password_hint_answers table
				getJdbcTemplatePrism().update(IQueryConstants.DELETE_ANSWER_DATA, Id);

				// delete the roles assigned to the user from user_role table
				getJdbcTemplatePrism().update(IQueryConstants.DELETE_USER_ROLE, Id);				
				
				// delete the user from users table
				getJdbcTemplatePrism().update(IQueryConstants.DELETE_USER, Id);
				
				// delete user from LDAP
				ldapManager.deleteUser(userName, userName, userName);

			//} else {

				//return false;
			//}

		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while deleting user details.", e);
			return false;
		}
		return true;
	}

	/**
	 * add new user
	 * @param String userId, String tenantId, String userName,
			String emailId, String password, String userStatus,
			String[] userRoles
	 * @return UserTO
	 */
	@TriggersRemove(cacheName="orgUsers", removeAll=true)
	public UserTO addNewUser(String userName, String tenantId, String userDisplayName,
			String emailId, String password, String userStatus,
			String[] userRoles,String orgLevel, String adminYear) throws BusinessException, Exception {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - addNewUser");
		UserTO to = null;
		try {
			if (!ldapManager.searchUser(userName)) {

				List<Map<String, Object>> userMap = getJdbcTemplatePrism()
						.queryForList(IQueryConstants.CHECK_EXISTING_USER,
								userName);
				if (userMap == null || userMap.isEmpty()) {
					if(ldapManager.addUser(userName, userName, userName, password)) {
						long user_seq_id = getJdbcTemplatePrism().queryForLong(
								IQueryConstants.USER_SEQ_ID);
						if (user_seq_id != 0) {
							getJdbcTemplatePrism()
									.update(IQueryConstants.INSERT_USER,
											user_seq_id, userName, tenantId,
											userDisplayName, emailId, userStatus, adminYear, orgLevel, IApplicationConstants.FLAG_Y);
							if (userRoles != null) {
								getJdbcTemplatePrism().update(
										IQueryConstants.INSERT_USER_ROLE, IApplicationConstants.DEFAULT_USER_ROLE,
										user_seq_id);
								for (String role : userRoles) {
									getJdbcTemplatePrism().update(
											IQueryConstants.INSERT_USER_ROLE, role,
											user_seq_id);
								}
							}
						}
						to = getEditUserData(String.valueOf(user_seq_id));
					}
					//ldapManager.addUser(userName, userName, userName, password);
				}
			}
		} catch (BusinessException bex) {
			throw new BusinessException(bex.getCustomExceptionMessage());
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while adding user details.", e);
			return null;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - addNewUser");
		return to;
	}

	/**
	 * Searches and returns the users(s) with given name (like operator).
	 * Performs case insensitive searching.
	 * 
	 * @param userName
	 *            Search String treated as organization name
	 * @param tenantId
	 *            parentId of the logged in user
	 * @return
	 */
	public ArrayList<UserTO> searchUser(String userName, String tenantId, String adminYear,String isExactSearch) {
		
		ArrayList<UserTO> UserTOs = new ArrayList<UserTO>();
		ArrayList<RoleTO> RoleTOs = new ArrayList<RoleTO>();
		List<Map<String, Object>> userslist = null;
		
		if (IApplicationConstants.FLAG_N.equalsIgnoreCase(isExactSearch)){
			userName = CustomStringUtil.appendString("%", userName, "%");
			//List<OrgTO> orgList = null;
			 userslist = getJdbcTemplatePrism().queryForList(
					IQueryConstants.SEARCH_USER, adminYear, tenantId, userName,userName,userName, adminYear, "15");
		}else{
			userslist = getJdbcTemplatePrism().queryForList(
					IQueryConstants.SEARCH_USER_EXACT, adminYear, tenantId, userName, adminYear, "15");
		}
		
		
		
		if (userslist.size() > 0) {
			UserTOs = new ArrayList<UserTO>();
			for (Map<String, Object> fieldDetails : userslist) {
				
				UserTO to = new UserTO();
				long userId=((BigDecimal) fieldDetails.get("USER_ID"))
				.longValue();
				to.setUserId(userId);
				/*to.setUserId(((BigDecimal) fieldDetails.get("USER_ID"))
						.longValue());*/
				to.setUserName((String) (fieldDetails.get("USERNAME")));
				to.setUserDisplayName((String) (fieldDetails.get("FULLNAME")));
				to.setStatus((String) (fieldDetails.get("STATUS")));
				to.setTenantId(((BigDecimal) fieldDetails.get("ORG_ID"))
						.longValue());
				to.setParentId(((BigDecimal) fieldDetails.get("ORG_PARENT_ID"))
						.longValue());
				to.setTenantName((String) (fieldDetails.get("ORG_NAME")));
				to.setUserType((String) (fieldDetails.get("USER_TYPE")));
				try {
					to.setLoggedInOrgId(Long.parseLong(tenantId));
				} catch (NumberFormatException e) {}
				
				//fetching role for each users
				if( (String.valueOf(userId) != null )&& ((String) (fieldDetails.get("USERNAME")) != null)) 
				{
					List<Map<String, Object>> roleList = null;
					roleList= getJdbcTemplatePrism().queryForList(
							IQueryConstants.GET_USER_ROLE, userId,IApplicationConstants.DEFAULT_USER_ROLE);
					if (roleList.size() > 0)
					{
						RoleTOs = new ArrayList<RoleTO>();
						for (Map<String, Object> roleDetails : roleList){
							
							RoleTO rleTo= new RoleTO();
							rleTo.setRoleId(((BigDecimal) roleDetails.get("ROLE_ID"))
									.longValue());
							rleTo.setRoleName((String) (roleDetails.get("ROLE_NAME")));
							
							RoleTOs.add(rleTo);
						}
						to.setAvailableRoleList(RoleTOs);
					}
									
				}
				
				
				
				UserTOs.add(to);
			}
		}

		return UserTOs;
	}
	/**
	 * Searches and returns the user names(use like operator) as a JSON
	 * string. Performs case insensitive searching. This method is used to
	 * perform auto complete in search box.
	 * 
	 * @param userName
	 *            Search String treated as organization name
	 * @param parentId
	 *            parentId of the logged in user
	 */
	public String searchUserAutoComplete(String userName, String tenantId, String adminYear){
		userName = CustomStringUtil.appendString("%", userName, "%");
		//List<OrgTO> userList = null;
		String userListJsonString = null;
		List<Map<String, Object>> listOfUser = getJdbcTemplatePrism().queryForList(
				IQueryConstants.SEARCH_USER, adminYear, tenantId, userName,userName,userName, adminYear, "100");
		if (listOfUser != null && listOfUser.size() > 0) {
			userListJsonString = "[";
			for (Map<String, Object> data : listOfUser) {
				//String orgNameStr = (String) data.get("USERNAME");
				userListJsonString = CustomStringUtil.appendString(
						userListJsonString, "\"", (String) data.get("USERNAME")
						, "<br/>", (String) data.get("FULLNAME"),"\",");
			}
			userListJsonString = CustomStringUtil.appendString(userListJsonString
					.substring(0, userListJsonString.length() - 1), "]");
		}
		logger.log(IAppLogger.DEBUG, userListJsonString);
		return userListJsonString;
	}
	/**
	 * Returns all organization list depending on tenantId
	 * @param String tenantId
	 * @return list of orgTO
	 */
	public List<OrgTO> getOrganizationList(String tenantId, String adminYear) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - getOrganizationList");
		
		List<Map<String, Object>> lstData = getJdbcTemplatePrism().queryForList(
				IQueryConstants.GET_ORGANIZATION_LIST, tenantId,tenantId,adminYear, adminYear, tenantId,tenantId);
		List<OrgTO> orgList = getOrgList(lstData, adminYear);

		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - getOrganizationList");
		return orgList;
	}

	/**
	 * Returns all the children of an organization.
	 * @param String tenantId
	 * @return list of orgTO
	 */
	@Cacheable(cacheName="orgChildren")
	public List<OrgTO> getOrganizationChildren(String nodeId, String adminYear, String searchParam) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - getOrganizationChildren");
		String parentTenantId = "";
		String orgId="";
		List<OrgTO> orgList=null;
		List<Map<String, Object>> lstData = null;
		
		if (nodeId.indexOf("_") > 0) {
			orgId = nodeId.substring((nodeId.indexOf("_") + 1),
					nodeId.length());
			parentTenantId = nodeId.substring(0, nodeId.indexOf("_"));
			logger.log(IAppLogger.DEBUG, "orgId is ................."+ orgId);
			if(searchParam != null && searchParam.trim().length() > 0) {
				searchParam = CustomStringUtil.appendString("%", searchParam, "%");
				lstData = getJdbcTemplatePrism().queryForList(IQueryConstants.GET_ORGANIZATION_CHILDREN_LIST_ON_SCROLL_WITH_SRCH_PARAM,
						parentTenantId,adminYear,adminYear,parentTenantId,parentTenantId,orgId, adminYear, searchParam);
			} else {
				lstData = getJdbcTemplatePrism().queryForList(IQueryConstants.GET_ORGANIZATION_CHILDREN_LIST_ON_SCROLL,
						parentTenantId,adminYear,adminYear,parentTenantId,parentTenantId,orgId, adminYear);
			}
			orgList = getOrgList(lstData, adminYear);
		} else {
			parentTenantId = nodeId;
			lstData = getJdbcTemplatePrism().queryForList(IQueryConstants.GET_ORGANIZATION_CHILDREN_LIST, parentTenantId, adminYear,adminYear,parentTenantId,parentTenantId,adminYear);
			orgList = getOrgList(lstData, adminYear);
			logger.log(IAppLogger.DEBUG, lstData.size()+"");
		}
		
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - getOrganizationChildren");
		return orgList;
	}

	/**
	 * Get all org list
	 * @param query
	 * @param tenantId
	 * @return
	 * 
	 */
	private List<OrgTO> getOrgList(List<Map<String, Object>> queryData, String adminYear) {
		List<OrgTO> orgList = null;
		if (queryData != null && queryData.size() > 0) {
			orgList = new ArrayList<OrgTO>();
			for (Map<String, Object> data : queryData) {
				OrgTO orgTO = new OrgTO();
				long orgId=((BigDecimal) data.get("ORG_ID")).longValue();
				orgTO.setTenantId(orgId);
				orgTO.setTenantName((String) data.get("ORG_NAME"));
				orgTO.setNoOfChildOrgs(((BigDecimal) data.get("CHILD_ORG_NO"))
						.longValue());
				orgTO.setParentTenantId(((BigDecimal) data.get("ORG_PARENT_ID"))
						.longValue());
			    orgTO.setSelectedOrgId((String) data.get("SELECTED_ORG_ID"));
				//orgTO.setNoOfUsers(((BigDecimal) data.get("USER_NO")).longValue());
			    //orgTO.setNoOfUsers(getTotalUserCount(orgId, adminYear));
				orgList.add(orgTO);
			}
		}
		return orgList;
	}
	/**
	 * Counts the number of users down the organisation
	 * hierarchy * 
	 *
	 * @param tenantId
	 *            tenantID of the user returned for a selected organisation
	 * @return
	 */
	
	public  OrgTO getTotalUserCount( String tenantId, String adminYear) {
		OrgTO orgTO = null;
		Map<String, Object> userCount = getJdbcTemplatePrism().queryForMap(
				IQueryConstants.GET_USER_COUNT,adminYear,tenantId, adminYear,adminYear);
		if (userCount != null && userCount.size() > 0){
			
			orgTO = new OrgTO();
			orgTO.setNoOfUsers(((BigDecimal) userCount.get("USER_NO")).longValue());
			orgTO.setAdminName((String) userCount.get("ADMIN_NAME"));
			
		
		}
		return orgTO;
	}
	/**
	 * Searches and returns the organization(s) with given name (like operator).
	 * Performs case insensitive searching.
	 * 
	 * @param orgName
	 *            Search String treated as organization name
	 * @param tenantId
	 *            tenantID of the logged in user
	 * @return
	 */
	public List<OrgTO> searchOrganization(String orgName, String tenantId, String adminYear) {
		orgName = CustomStringUtil.appendString("%", orgName, "%");
		List<OrgTO> orgList = null;
		List<Map<String, Object>> list = getJdbcTemplatePrism().queryForList(
				IQueryConstants.SEARCH_ORGANNIZATION,adminYear, orgName, adminYear, tenantId,adminYear,tenantId);
		if (list != null && list.size() > 0) {
			orgList = new ArrayList<OrgTO>();
			for (Map<String, Object> data : list) {
				OrgTO orgTO = new OrgTO();
				orgTO.setTenantId(((BigDecimal) data.get("ORG_ID")).longValue());
				orgTO.setTenantName((String) data.get("ORG_NAME"));
				orgTO.setNoOfChildOrgs(((BigDecimal) data.get("CHILD_ORG_NO"))
						.longValue());
				//orgTO.setNoOfUsers(((BigDecimal) data.get("USER_NO")).longValue());
						
				orgList.add(orgTO);
			}
		}
		return orgList;
	}

	/**
	 * Searches and returns the organization names(use like operator) as a JSON
	 * string. Performs case insensitive searching. This method is used to
	 * perform auto complete in search box.
	 * 
	 * @param orgName
	 *            Search String treated as organization name
	 * @param tenantId
	 *            tenantID of the logged in user
	 */
	public String searchOrgAutoComplete(String orgName, String tenantId, String adminYear) {
		orgName = CustomStringUtil.appendString("%", orgName, "%");
		//List<OrgTO> orgList = null;
		String orgListJsonString = null;
		List<Map<String, Object>> list = getJdbcTemplatePrism().queryForList(
				IQueryConstants.SEARCH_ORG_AUTO_COMPLETE, adminYear, tenantId, orgName);
		if (list != null && list.size() > 0) {
			orgListJsonString = "[";
			for (Map<String, Object> data : list) {
				String orgNameStr = (String) data.get("ORG_NAME");
				orgListJsonString = CustomStringUtil.appendString(
						orgListJsonString, "\"", orgNameStr, "\",");
			}
			orgListJsonString = CustomStringUtil.appendString(orgListJsonString
					.substring(0, orgListJsonString.length() - 1), "]");
		}
		return orgListJsonString;
	}

	/**
	 * Get all roles 
	 * @param 
	 * @return list of role
	 */
	public ArrayList<RoleTO> getRoleDetails() {

		ArrayList<RoleTO> RoleTOs = new ArrayList<RoleTO>();

		List<Map<String, Object>> lstData = null;

		lstData = getJdbcTemplatePrism().queryForList(
				IQueryConstants.GET_ROLE_DETAILS);

		if (lstData.size() > 0) {
			RoleTOs = new ArrayList<RoleTO>();
			for (Map<String, Object> fieldDetails : lstData) {
				RoleTO to = new RoleTO();

				to.setRoleId(((BigDecimal) fieldDetails.get("ROLE_ID"))
						.longValue());
				to.setRoleName((String) (fieldDetails.get("ROLE_NAME")));
				to.setRoleDescription((String) (fieldDetails.get("DESCRIPTION")));
				RoleTOs.add(to);
			}
		}
		return RoleTOs;
	}

	/**
	 * Get role details
	 * @param role
	 * @return RoleTO
	 */
	public RoleTO getRoleDetailsById(String roleid) {

		RoleTO roleTO = new RoleTO();

		List<Map<String, Object>> lstData = null;
		lstData = getJdbcTemplatePrism().queryForList(
				IQueryConstants.GET_ROLE_DETAILS_BY_ID, roleid);

		if (lstData.size() > 0) {

			for (Map<String, Object> fieldDetails : lstData) {

				roleTO.setRoleId(((BigDecimal) fieldDetails.get("ROLE_ID"))
						.longValue());
				roleTO.setRoleName((String) (fieldDetails.get("ROLE_NAME")));
				roleTO.setRoleDescription((String) (fieldDetails.get("DESCRIPTION")));

			}
		}
		roleTO.setUserList(getUsersForSelectedRole(roleid));

		return roleTO;
	}

	/**
	 * get user list for selected role
	 * @param role id
	 * @return List of users
	 */
	public ArrayList<UserTO> getUsersForSelectedRole(String roleid) {

		ArrayList<UserTO> UserTOs = new ArrayList<UserTO>();

		List<Map<String, Object>> lstData = null;
		lstData = getJdbcTemplatePrism().queryForList(
				IQueryConstants.GET_USERS_FOR_SELECTED_ROLE, roleid);
		logger.log(IAppLogger.DEBUG, lstData.size()+"");

		if (lstData.size() > 0) {

			for (Map<String, Object> fieldDetails : lstData) {
				UserTO to = new UserTO();
				to.setUserId(((BigDecimal) fieldDetails.get("USER_ID"))
						.longValue());
				to.setUserName((String) (fieldDetails.get("USERNAME")));
				UserTOs.add(to);
			}
		}
		return UserTOs;
	}

	/**
	 * Update role details
	 * @param roleTo
	 * @return
	 */
	public boolean updateRole(RoleTO roleTo) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - updateRole");
		try {
			ArrayList<UserTO> userTOs = new ArrayList<UserTO>();
			long roleId = roleTo.getRoleId();
			String roleName = roleTo.getRoleName();
			String roleDescription = roleTo.getRoleDescription();

			// update role table
			getJdbcTemplatePrism().update(IQueryConstants.UPDATE_ROLE,
					roleName, roleDescription, roleId);

			if (roleTo.getUserList().size() > 0) {
				// delete users from user role table
				getJdbcTemplatePrism().update(
						IQueryConstants.DELETE_ROLE_FROM_USER_ROLE_TABLE,
						roleId);
				userTOs = (ArrayList<UserTO>) roleTo.getUserList();
				for (UserTO userTo : userTOs) {
					long userId = userTo.getUserId();
					// insert users into user role table
					getJdbcTemplatePrism().update(
							IQueryConstants.INSERT_INTO_USER_ROLE, userId,
							roleId);
				}
			}
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while updating role details.", e);
			return false;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - updateRole");
		return true;
	}

	/**
	 * Delete selected role
	 * @param
	 * @return
	 */
	public boolean deleteRole(String roleid) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - deleteRole");
		try {
			// removing the role from the users that is to be deleted
				getJdbcTemplatePrism().update(
						IQueryConstants.DELETE_ROLE_FROM_USER_ROLE_TABLE, roleid);
				
			// deleting the roles from the roles table
			getJdbcTemplatePrism().update(
					IQueryConstants.DELETE_ROLE_FROM_ROLES_TABLE, roleid);

		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while deleting role table.", e);
			return false;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - deleteRole");
		return true;
	}
	/**
	 * Associate user for that role in database through associate button in edit role popup screen
	 * @param roleName
	 * @param roleDescription
	 * @param roleId
	 */
	public boolean associateUserToRole(String roleId, String userName) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - associateUserToRole");
		try {			
			// update role table
			getJdbcTemplatePrism().update(IQueryConstants.INSERT_INTO_USER_ROLE, userName, roleId);
			
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while associating user to role.", e);
			return false;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - associateUserToRole");
		return true;
	}
	
	/**
	 * Delete user for that role in database through delete button in edit role popup screen
	 * @param roleId
	 * @param userId
	 */
	public boolean deleteUserFromRole(String roleId, String userId) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - deleteUserFromRole");
		try {			
			// update role table
			getJdbcTemplatePrism().update(IQueryConstants.DELETE_USER_FROM_USER_ROLE_TABLE, roleId, userId);
			
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while deleting user for role.", e);
			return false;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - deleteUserFromRole");
		return true;
	}
	
	/**
	 * Updates the particular role information in database through save button in edit role popup screen
	 * @param roleName
	 * @param roleDescription
	 * @param roleId
	 */
	public boolean saveRole(String roleId, String roleName, String roleDescription) {
		logger.log(IAppLogger.INFO, "Enter: AdminDAOImpl - updateRole");
		try {			
			// update role table
			getJdbcTemplatePrism().update(IQueryConstants.UPDATE_ROLE, roleName, roleDescription, roleId);
			
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error occurred while updating role details.", e);
			return false;
		}
		logger.log(IAppLogger.INFO, "Exit: AdminDAOImpl - updateRole");
		return true;
	}
	
	/**
	 * Reset the user password into ldap
	 * @param roleName
	 * @param roleDescription
	 * @param roleId
	 */
	public String resetPassword(String userName) throws Exception {
		
		String password = PasswordGenerator.getNext();
		boolean isUpdated = ldapManager.updateUser(userName, userName, userName, password);
		if (isUpdated) {
			getJdbcTemplatePrism().update(IQueryConstants.UPDATE_FIRSTTIMEUSERFLAG_DATA,
					IApplicationConstants.FLAG_Y,userName);
						
			return password;
		} else {
			return null;
		}
		
		
	}
	
	/**
	 * get user list for selected role
	 * @param role id
	 * @return List of users
	 */
	@Cacheable(cacheName="allAdminYear")
	public List<ObjectValueTO> getAllAdmin() {
		List<ObjectValueTO> adminYearList = new ArrayList<ObjectValueTO>();
		List<Map<String, Object>> lstData = null;
		lstData = getJdbcTemplatePrism().queryForList(IQueryConstants.ADMIN_YEAR_LIST);
		logger.log(IAppLogger.DEBUG, lstData.size()+"");
		if (lstData.size() > 0) {
			for (Map<String, Object> fieldDetails : lstData) {
				ObjectValueTO to = new ObjectValueTO();
				to.setValue( ((BigDecimal) (fieldDetails.get("ADMINID"))).toString() );
				to.setName((String) (fieldDetails.get("ADMIN_NAME")));
				adminYearList.add(to);
			}
		}
		return adminYearList;
	}
}
