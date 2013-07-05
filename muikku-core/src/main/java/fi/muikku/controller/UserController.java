package fi.muikku.controller;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.muikku.dao.base.EnvironmentDefaultsDAO;
import fi.muikku.dao.users.EnvironmentUserDAO;
import fi.muikku.dao.users.EnvironmentUserRoleDAO;
import fi.muikku.dao.users.UserContactDAO;
import fi.muikku.dao.users.UserEntityDAO;
import fi.muikku.dao.users.UserGroupDAO;
import fi.muikku.dao.users.UserGroupUserDAO;
import fi.muikku.dao.users.UserPictureDAO;
import fi.muikku.events.Archived;
import fi.muikku.events.Created;
import fi.muikku.events.Modified;
import fi.muikku.events.UserEntityEvent;
import fi.muikku.model.users.EnvironmentUser;
import fi.muikku.model.users.UserContact;
import fi.muikku.model.users.UserEntity;
import fi.muikku.model.users.UserGroup;
import fi.muikku.schooldata.UserSchoolDataController;
import fi.muikku.schooldata.entity.User;
import fi.muikku.security.MuikkuPermissions;
import fi.muikku.security.Permit;
import fi.muikku.session.SessionController;

@Dependent
public class UserController {

	@Inject
	private UserSchoolDataController userSchoolDataController;
	
  @Inject
  private EnvironmentUserDAO environmentUserDAO;
  
  @Inject
  private EnvironmentUserRoleDAO environmentUserRoleDAO;
  
  @Inject
  private SessionController sessionController;
  
  @Inject 
  private EnvironmentDefaultsDAO environmentDefaultsDAO;

  @Inject
  private UserPictureDAO userPictureDAO;
  
  @Inject
  private UserContactDAO userContactDAO;
  
  @Inject
  private UserEntityDAO userEntityDAO;

  @Inject
  private UserGroupDAO userGroupDAO;
  
  @Inject
  private UserGroupUserDAO userGroupUserDAO;
  
  @Inject
  @Created
  private Event<UserEntityEvent> userCreatedEvent;
  
  @Inject
  @Modified
  private Event<UserEntityEvent> userModifiedEvent;

  @Inject
  @Archived
  private Event<UserEntityEvent> userRemovedEvent;
    
  public UserEntity findUserEntity(Long userEntityId) {
    return userEntityDAO.findById(userEntityId);
  }

  public boolean getUserHasPicture(UserEntity user) {
    return userPictureDAO.findUserHasPicture(user);
  }
  
  public List<UserContact> listUserContacts(UserEntity user) {
    boolean hidden = false;
    
    return userContactDAO.listByUser(user, hidden);
  }
  
  public List<UserGroup> searchUserGroups(String searchTerm) {
    List<UserGroup> grps = userGroupDAO.listAll();
    List<UserGroup> filtered = new ArrayList<UserGroup>();
    
    for (UserGroup grp : grps) {
      if (grp.getName().toLowerCase().contains(searchTerm.toLowerCase()))
        filtered.add(grp);
    }
    
    return filtered;
  }
  
  // TODO: Move to createUser and registration widget bean

//  public void registerUser(SchoolDataSource dataSource, String firstName, String lastName, String email, String passwordHash) {
//    Environment environment = sessionController.getEnvironment();
//
//    UserEntity userEntity = userEntityDAO.create(dataSource, false);
//    
//    /**
//     * Create User    
//     */
//    userController.createUser(userEntity, firstName, lastName, email);
//    userPasswordDAO.create(userEntity, DigestUtils.md5Hex(passwordHash));
//    
//    /**
//     * Give User Student Role
//     */
//    // TODO
//    EnvironmentDefaults defaults = environmentDefaultsDAO.findByEnvironment(sessionController.getEnvironment());
//
//    EnvironmentUserRole userRole = defaults.getDefaultUserRole();
//    environmentUserDAO.create(userEntity, environment, userRole);
//
//    fireUserCreatedEvent(userEntity);
//  }

  @Permit (MuikkuPermissions.MANAGE_USERS) // TODO: ???
  public List<EnvironmentUser> listEnvironmentUsers() {
    return environmentUserDAO.listAll();
  }

  public List<EnvironmentUser> searchUsers(String searchTerm) {
    List<EnvironmentUser> users = environmentUserDAO.listAll();
    List<EnvironmentUser> filtered = new ArrayList<EnvironmentUser>();
    
    for (EnvironmentUser u : users) {
      User user = userSchoolDataController.findUser(u.getUser());
      String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
      
      if (fullName.contains(searchTerm))
        filtered.add(u);
    }
    
    return filtered;
  }
  
  public Long getUserGroupMemberCount(UserGroup userGroup) {
    return userGroupUserDAO.countByUserGroup(userGroup);
  }

  private void fireUserCreatedEvent(UserEntity userEntity) {
    UserEntityEvent userEvent = new UserEntityEvent();
    userEvent.setUserEntityId(userEntity.getId());
    userCreatedEvent.fire(userEvent);
  }

  private void fireUserModifiedEvent(UserEntity userEntity) {
    UserEntityEvent userEvent = new UserEntityEvent();
    userEvent.setUserEntityId(userEntity.getId());
    userModifiedEvent.fire(userEvent);
  }

  private void fireUserRemovedEvent(UserEntity userEntity) {
    UserEntityEvent userEvent = new UserEntityEvent();
    userEvent.setUserEntityId(userEntity.getId());
    userRemovedEvent.fire(userEvent);
  }
}
