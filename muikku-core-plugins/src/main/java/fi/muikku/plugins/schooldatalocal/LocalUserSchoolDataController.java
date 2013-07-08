package fi.muikku.plugins.schooldatalocal;

import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.muikku.dao.DAO;
import fi.muikku.plugins.schooldatalocal.dao.LocalUserDAO;
import fi.muikku.plugins.schooldatalocal.dao.LocalUserEmailDAO;
import fi.muikku.plugins.schooldatalocal.dao.LocalUserImageDAO;
import fi.muikku.plugins.schooldatalocal.dao.LocalUserPropertyDAO;
import fi.muikku.plugins.schooldatalocal.dao.LocalUserPropertyKeyDAO;
import fi.muikku.plugins.schooldatalocal.model.LocalUser;
import fi.muikku.plugins.schooldatalocal.model.LocalUserEmail;
import fi.muikku.plugins.schooldatalocal.model.LocalUserImage;
import fi.muikku.plugins.schooldatalocal.model.LocalUserProperty;
import fi.muikku.plugins.schooldatalocal.model.LocalUserPropertyKey;

@Dependent
@Stateful
public class LocalUserSchoolDataController {
	
	public static final String SCHOOL_DATA_SOURCE = "LOCAL";
	
	@DAO
	@Inject
	private LocalUserDAO localUserDAO;
	
	@DAO
	@Inject
	private LocalUserEmailDAO localUserEmailDAO;

	@DAO
	@Inject
	private LocalUserImageDAO localUserImageDAO;

	@DAO
	@Inject
	private LocalUserPropertyKeyDAO localUserPropetyKeyDAO;

	@DAO
	@Inject
	private LocalUserPropertyDAO localUserPropertyDAO;

	public LocalUser createUser(String firstName, String lastName) {
		return localUserDAO.create(firstName, lastName, Boolean.FALSE);
	}

	public LocalUser findUser(String identifier) {
		return localUserDAO.findById(NumberUtils.createLong(identifier));
	}

	public LocalUser findUserByEmail(String email) {
		LocalUserEmail localUserEmail = localUserEmailDAO.findByAddressAndArchived(email, Boolean.FALSE);
		if (localUserEmail != null) {
			return localUserEmail.getUser();
		}

		return null;
	}

	public List<LocalUser> listUsers() {
		return localUserDAO.listAll();
	}

	public LocalUser updateUserFirstName(LocalUser localUser, String firstName) {
		return localUserDAO.updateFirstName(localUser, firstName);
	}

	public LocalUser updateUserLastName(LocalUser localUser, String lastName) {
		return localUserDAO.updateLastName(localUser, lastName);
	}

	public void removeUser(String identifier) {
		LocalUser localUser = findUser(identifier);
		if (localUser != null) {
			localUserDAO.archive(localUser);
		}
	}

	public LocalUserEmail createUserEmail(String userIdentifier, String address) {
		// TODO: Proper Error Handling
		
		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			return localUserEmailDAO.create(localUser, address, Boolean.FALSE);
		}
 		
		return null;
	}

	public LocalUserEmail findUserEmail(String identifier) {
		return localUserEmailDAO.findById(NumberUtils.createLong(identifier));
	}

	public List<LocalUserEmail> listUserEmailsByUserIdentifier(String userIdentifier) {
		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
		  return localUserEmailDAO.listByUserAndArchived(localUser, Boolean.FALSE);
		}

		return null;
	}

	public LocalUserEmail updateUserEmail(String identifier, String address) {
		LocalUserEmail localUserEmail = findUserEmail(identifier);
		if (localUserEmail != null) {
			return localUserEmailDAO.updateAddress(localUserEmail, address);
		}
		
		return null;
	}

	public void removeUserEmail(String identifier) {
		LocalUserEmail localUserEmail = findUserEmail(identifier);
		if (localUserEmail != null) {
			localUserEmailDAO.archive(localUserEmail);
		}
	}

	public LocalUserImage createUserImage(String userIdentifier, String contentType, byte[] data) {
		// TODO: Proper Error Handling
		
		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			return localUserImageDAO.create(localUser, contentType, data);
		}
 		
		return null;
	}

	public LocalUserImage findUserImage(String identifier) {
		return localUserImageDAO.findById(NumberUtils.createLong(identifier));
	}

	public List<LocalUserImage> listUserImagesByUserIdentifier(String userIdentifier) {
	  // TODO: Proper Error Handling
		
		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			return localUserImageDAO.listByUser(localUser);
		}
	
		return null;
	}

	public LocalUserImage updateUserImage(String identifier, String contentType, byte[] content) {
	  // TODO: Proper Error Handling

		LocalUserImage localUserImage = findUserImage(identifier);
		if (localUserImage != null) {
			localUserImageDAO.updateContentType(localUserImage, contentType);
			return localUserImageDAO.updateData(localUserImage, content);
		}
	
		return null;
	}

	public void removeUserImage(String identifier) {
	  // TODO: Proper Error Handling

		LocalUserImage localUserImage = findUserImage(identifier);
		if (localUserImage != null) {
			localUserImageDAO.delete(localUserImage);
		}
	}

	public LocalUserProperty findUserProperty(String identifier) {
		return localUserPropertyDAO.findById(NumberUtils.createLong(identifier));
	}
	
	public LocalUserProperty createUserProperty(String userIdentifier, String key, String value) {
	  // TODO: Proper Error Handling

		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			LocalUserPropertyKey propertyKey = getLocalUserPropertyKey(key, true);
			return localUserPropertyDAO.create(localUser, propertyKey, value);
		}
		
		return null;
	}

	public LocalUserProperty findUserPropertyByUserAndKey(String userIdentifier, String key) {
	  // TODO: Proper Error Handling

		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			LocalUserPropertyKey propertyKey = getLocalUserPropertyKey(key, false);
			if (propertyKey != null) {
				return localUserPropertyDAO.findByUserAndKey(localUser, propertyKey);
			}
		}
		
		return null;
  }

	public List<LocalUserProperty> listUserPropertiesByUser(String userIdentifier) {
		// TODO: Proper Error Handling

		LocalUser localUser = findUser(userIdentifier);
		if (localUser != null) {
			return localUserPropertyDAO.listByUser(localUser);
		}
		
		return null;
	}

	public LocalUserProperty updateUserProperty(String userIdentifier, String key, String value) {
		// TODO: Proper Error Handling

		LocalUserProperty localUserProperty = findUserPropertyByUserAndKey(userIdentifier, key);
		if (localUserProperty != null) {
			return localUserPropertyDAO.updateValue(localUserProperty, value);
		}
			
		return null;
	}

	public void removeUserProperty(String identifier) {
		LocalUserProperty localUserProperty = findUserProperty(identifier);
		if (localUserProperty != null) {
			localUserPropertyDAO.delete(localUserProperty);
		}
	}

	private LocalUserPropertyKey getLocalUserPropertyKey(String name, boolean createMissing) {
		LocalUserPropertyKey userPropertyKey = localUserPropetyKeyDAO.findByNameAndArchived(name, Boolean.FALSE);
		if (userPropertyKey != null) {
			return userPropertyKey;
		}
		
		if (createMissing) {
		  return localUserPropetyKeyDAO.create(name, Boolean.FALSE);
		} else {
			return null;
		}
	}

}
