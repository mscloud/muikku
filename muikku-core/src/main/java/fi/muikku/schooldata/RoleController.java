package fi.muikku.schooldata;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.muikku.dao.base.SchoolDataSourceDAO;
import fi.muikku.dao.users.RoleEntityDAO;
import fi.muikku.dao.users.RoleSchoolDataIdentifierDAO;
import fi.muikku.dao.workspace.WorkspaceRoleEntityDAO;
import fi.muikku.model.base.SchoolDataSource;
import fi.muikku.model.users.RoleEntity;
import fi.muikku.model.users.RoleSchoolDataIdentifier;
import fi.muikku.model.workspace.WorkspaceRoleArchetype;
import fi.muikku.model.workspace.WorkspaceRoleEntity;
import fi.muikku.schooldata.entity.Role;
import fi.muikku.schooldata.entity.User;

public class RoleController {

	@Inject
	private Logger logger;

	@Inject
	private SchoolDataSourceDAO schoolDataSourceDAO;
	
	@Inject
	private UserSchoolDataController userSchoolDataController;

  @Inject
  private RoleEntityDAO roleEntityDAO;
  
  @Inject
  private WorkspaceRoleEntityDAO workspaceRoleEntityDAO;

  @Inject
  private RoleSchoolDataIdentifierDAO roleSchoolDataIdentifierDAO;
  
  /* Roles */

	public Role findRole(SchoolDataSource dataSource, String identifier) {
		return userSchoolDataController.findRole(dataSource,identifier);
	}

  public Role findRoleByDataSourceAndRoleEntity(String schoolDataSource, RoleEntity roleEntity) {
    SchoolDataSource dataSource = schoolDataSourceDAO.findByIdentifier(schoolDataSource);
    if (dataSource == null) {
      logger.severe("Could not find school data source " + schoolDataSource);
      return null;
    }
      
    RoleSchoolDataIdentifier schoolDataIdentifier = roleSchoolDataIdentifierDAO.findByDataSourceAndRoleEntity(dataSource, roleEntity);
    if (schoolDataIdentifier != null) {
      return findRole(dataSource, schoolDataIdentifier.getIdentifier());
    }

    return null;
  }
  
	public Role findUserEnvironmentRole(User user) {
		return userSchoolDataController.findUserEnvironmentRole(user);
	}

	public List<Role> listRoles() {
		return userSchoolDataController.listRoles();
	}
	
	public void setRoleEntity(String schoolDataSource, String identifier, RoleEntity roleEntity) {
		SchoolDataSource dataSource = schoolDataSourceDAO.findByIdentifier(schoolDataSource);
		if (dataSource != null) {
		  RoleSchoolDataIdentifier roleSchoolDataIdentifier = roleSchoolDataIdentifierDAO.findByDataSourceAndIdentifier(dataSource, identifier);
		  if (roleSchoolDataIdentifier != null) {
		  	if (roleEntity != null) {
		  	  roleSchoolDataIdentifierDAO.updateRoleEntity(roleSchoolDataIdentifier, roleEntity);
		  	} else {
		  		roleSchoolDataIdentifierDAO.delete(roleSchoolDataIdentifier);
		  	}
		  } else {
		  	if (roleEntity != null) {
		  		roleSchoolDataIdentifierDAO.create(dataSource, identifier, roleEntity);
		  	}		  	
		  }
		}
	}

	/* Role Entities */

	public RoleEntity findRoleEntityById(Long id) {
		return roleEntityDAO.findById(id);
	}

	public RoleEntity findRoleEntityByDataSourceAndIdentifier(SchoolDataSource schoolDataSource, String identifier) {
	  RoleSchoolDataIdentifier schoolDataIdentifier = roleSchoolDataIdentifierDAO.findByDataSourceAndIdentifier(schoolDataSource, identifier);
	  if (schoolDataIdentifier != null) {
	  	return schoolDataIdentifier.getRoleEntity();
	  }
		
		return null;
	}

  public RoleEntity findRoleEntityByDataSourceAndIdentifier(String dataSource, String identifier) {
    SchoolDataSource schoolDataSource = schoolDataSourceDAO.findByIdentifier(dataSource);
    if (schoolDataSource != null) {
      return findRoleEntityByDataSourceAndIdentifier(schoolDataSource, identifier);
    } else {
      logger.severe("Could not find school data source: " + dataSource);
      return null;
    }
  }

  public RoleEntity findRoleEntity(Role role) {
    return findRoleEntityByDataSourceAndIdentifier(role.getSchoolDataSource(), role.getIdentifier());
  }
	
	public List<RoleEntity> listRoleEntities() {
		return roleEntityDAO.listAll();
	}

	/* Workspace Role Entities */
	
	public List<WorkspaceRoleEntity> listWorkspaceRoleEntities() {
		return workspaceRoleEntityDAO.listAll();
	}

  public WorkspaceRoleEntity findWorkspaceRoleEntityById(Long id) {
    return workspaceRoleEntityDAO.findById(id);
  }
	
	public WorkspaceRoleEntity findWorkspaceRoleEntityByName(String name) {
	  return workspaceRoleEntityDAO.findByName(name);
	}
	
	public WorkspaceRoleEntity findWorkspaceRoleEntity(Role role) {
		RoleEntity roleEntity = findRoleEntity(role);
		if (roleEntity instanceof WorkspaceRoleEntity) {
			return (WorkspaceRoleEntity) roleEntity;
		}
		
		return null;
	}
  
  public List<WorkspaceRoleEntity> listWorkspaceRoleEntitiesByArchetype(WorkspaceRoleArchetype archetype) {
    return workspaceRoleEntityDAO.listByArchetype(archetype);
  }
}
