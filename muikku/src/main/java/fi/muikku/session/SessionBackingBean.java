package fi.muikku.session;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import fi.muikku.model.users.EnvironmentRoleArchetype;
import fi.muikku.model.users.EnvironmentUser;
import fi.muikku.model.users.UserEntity;
import fi.muikku.schooldata.entity.User;
import fi.muikku.users.EnvironmentUserController;
import fi.muikku.users.UserController;

@RequestScoped
@Named
@Stateful
public class SessionBackingBean {

  @Inject
  private EnvironmentUserController environmentUserController;

  @Inject
	private SessionController sessionController;

  @Inject
	private UserController userController;
	
	@PostConstruct
	public void init() {
	  loggedUserRoleArchetype = null;
	  loggedUserName = null;

    if (sessionController.isLoggedIn()) {
	    UserEntity loggedUser = sessionController.getUser();
	    if (loggedUser != null) {
	      EnvironmentUser environmentUser = environmentUserController.findEnvironmentUserByUserEntity(loggedUser);
	      if (environmentUser != null) {
	        loggedUserRoleArchetype = environmentUser.getRole().getArchetype();
	      }
	    }
	    
	    User user = userController.findUserByUserEntity(loggedUser);
	    if (user != null) {
	      loggedUserName = user.getFirstName() + ' ' + user.getLastName();
	    }
	  }
	}
	
	public boolean getLoggedIn() {
		return sessionController.isLoggedIn();
	}
	
	public Long getLoggedUserId() {
	  return sessionController.isLoggedIn() ? sessionController.getUser().getId() : null;
	}

	public String getResourceLibrary() {
		return "theme-muikku";
	}
	
	public boolean getTeacherLoggedIn() {
    return loggedUserRoleArchetype == EnvironmentRoleArchetype.TEACHER;
  }
	
	public boolean getManagerLoggedIn() {
    return loggedUserRoleArchetype == EnvironmentRoleArchetype.MANAGER;
  }
	
	public boolean getAdministratorLoggedIn() {
    return loggedUserRoleArchetype == EnvironmentRoleArchetype.ADMINISTRATOR;
  }
	
	public boolean getStudentLoggedIn() {
	  return loggedUserRoleArchetype == EnvironmentRoleArchetype.STUDENT;
	}
	
	public String getLoggedUserName() {
	  return loggedUserName;
	}
	
	private EnvironmentRoleArchetype loggedUserRoleArchetype;
	private String loggedUserName;
}
