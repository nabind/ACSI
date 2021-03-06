/**
 * 
 */
package com.ctb.prism.admin.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ctb.prism.admin.dao.IAdminDAO;
import com.ctb.prism.admin.transferobject.ObjectValueTO;
import com.ctb.prism.admin.transferobject.OrgTO;
import com.ctb.prism.admin.transferobject.OrgTreeTO;
import com.ctb.prism.admin.transferobject.RoleTO;
import com.ctb.prism.admin.transferobject.UserTO;
import com.ctb.prism.core.exception.BusinessException;

/**
 * @author TCS
 * 
 */

@Component("adminBusiness")
public class AdminBusinessImpl implements IAdminBusiness {

	@Autowired
	private IAdminDAO adminDAO;

	public ArrayList<OrgTO> getOrganizationDetailsOnFirstLoad(String nodeid)
		throws Exception {

		return adminDAO.getOrganizationDetailsOnFirstLoad(nodeid);

	}
	
	public ArrayList<OrgTO> getOrganizationDetailsOnClick(String nodeid)
			throws Exception {

		return adminDAO.getOrganizationDetailsOnClick(nodeid);

	}

	public ArrayList<UserTO> getUserDetailsOnClick(String nodeid,String currorg, String adminYear, String searchParam)
			throws Exception {

		return adminDAO.getUserDetailsOnClick(nodeid,currorg,adminYear, searchParam);

	}

	public UserTO getEditUserData(String nodeid) throws Exception {

		return adminDAO.getEditUserData(nodeid);

	}

	public boolean updateUser(String Id, String userId, String userName,
			String emailId, String password, String userStatus,
			String[] userRoles) throws BusinessException, Exception {

		return adminDAO.updateUser(Id, userId, userName, emailId, password,
				userStatus, userRoles);
	}

	public boolean deleteUser(String Id, String userName, String password)
			throws Exception {

		return adminDAO.deleteUser(Id, userName, password);
	}

	public List<RoleTO> getRoleOnAddUser(String orgLevel) {

		return adminDAO.getRoleOnAddUser(orgLevel);

	}

	public UserTO addNewUser(String userId, String tenantId, String userName,
			String emailId, String password, String userStatus,
			String[] userRoles, String orgLevel, String adminYear) throws BusinessException, Exception {

		return adminDAO.addNewUser(userId, tenantId, userName, emailId,
				password, userStatus, userRoles, orgLevel, adminYear);
	}

	
	public ArrayList <UserTO> searchUser(String userName, String parentId, String adminYear,String isExactSearch){
		return adminDAO.searchUser( userName,  parentId, adminYear,isExactSearch);
	}
	
	public String searchUserAutoComplete( String userName, String parentId, String adminYear ) {
		return adminDAO.searchUserAutoComplete( userName, parentId, adminYear );
	}
	
	public List<OrgTO> getOrganizationList(String tenantId, String adminYear) {
		return adminDAO.getOrganizationList(tenantId, adminYear);
	}

	public List<OrgTO> getOrganizationChildren(String parentTenantId, String adminYear, String searchParam) {
		return adminDAO.getOrganizationChildren(parentTenantId, adminYear, searchParam);
	}

	public List<OrgTO> searchOrganization(String orgName, String tenantId, String adminYear) {
		return adminDAO.searchOrganization(orgName, tenantId, adminYear);
	}
	
	public OrgTO getTotalUserCount(String tenantId, String adminYear) {
		return adminDAO.getTotalUserCount(tenantId, adminYear);
	}


	public String searchOrgAutoComplete(String orgName, String tenantId, String adminYear) {
		return adminDAO.searchOrgAutoComplete(orgName, tenantId, adminYear);
	}

	public ArrayList<RoleTO> getRoleDetails() throws Exception {
		return adminDAO.getRoleDetails();
	}

	public ArrayList<UserTO> getUsersForSelectedRole(String roleid)
			throws Exception {
		return adminDAO.getUsersForSelectedRole(roleid);
	}

	public boolean deleteRole(String roleid) throws Exception {
		return adminDAO.deleteRole(roleid);
	}

	public RoleTO getRoleDetailsById(String roleid) throws Exception {
		return adminDAO.getRoleDetailsById(roleid);
	}

	
	public boolean associateUserToRole(String roleId, String userName) throws Exception{
		return  adminDAO.associateUserToRole(roleId, userName);
	}

	public boolean deleteUserFromRole(String roleId, String userId) throws Exception{
		return  adminDAO.deleteUserFromRole(roleId, userId);
	}

	public boolean saveRole(String roleId, String roleName, String roleDescription) throws Exception{
		return  adminDAO.saveRole(roleId, roleName, roleDescription);
	}
	public ArrayList<OrgTreeTO> getOrganizationTree(String nodeid,String currOrg,boolean flgFirstLoad, String adminYear)throws Exception 
	{
		return adminDAO.getOrganizationTree(nodeid,currOrg,flgFirstLoad, adminYear);
	}
	public ArrayList<OrgTreeTO> getOrgTree(String nodeid,boolean isFirstLoad, String adminYear)throws Exception {
		return adminDAO.getOrgTree(nodeid,isFirstLoad, adminYear);
	}
	public String getOrganizationTreeOnRedirect(String selectedOrgId,String parentOrgId,String userId,String userName, boolean isRedirected) throws Exception{
		return adminDAO.getOrganizationTreeOnRedirect(selectedOrgId,parentOrgId,userId,userName,isRedirected);
	}
	public String resetPassword(String userName) throws Exception {
		return adminDAO.resetPassword(userName);
	}
	
	public List<ObjectValueTO> getAllAdmin() throws Exception {
		return adminDAO.getAllAdmin();
	}
	

}
