package fi.muikku.plugins.internalauth;

import java.util.Arrays;
import java.util.Map;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.muikku.auth.AbstractAuthenticationStrategy;
import fi.muikku.auth.AuthenticationHandleException;
import fi.muikku.auth.AuthenticationProvider;
import fi.muikku.auth.AuthenticationResult;
import fi.muikku.auth.AuthenticationResult.Status;
import fi.muikku.model.security.AuthSource;
import fi.muikku.model.users.UserEntity;
import fi.muikku.plugins.internalauth.model.InternalAuth;
import fi.muikku.users.UserController;
import fi.muikku.users.UserEntityController;
import fi.muikku.schooldata.entity.User;

@Dependent
@Stateless
public class InternalAuthenticationStrategy extends AbstractAuthenticationStrategy implements AuthenticationProvider {
  
  @Inject
  private UserController userController;

  @Inject
  private UserEntityController userEntityController;
  
  @Inject
  private InternalAuthController internalLoginController;

  @Override
  public String getName() {
    return "internalauth";
  }

  @Override
  public boolean requiresCredentials() {
    return true;
  }

  @Override
  public AuthenticationResult processLogin(AuthSource authSource, Map<String, String[]> requestParameters) throws AuthenticationHandleException {
    String email = StringUtils.lowerCase(getFirstRequestParameter(requestParameters, "email"));
    String password = getFirstRequestParameter(requestParameters, "password");
    
    InternalAuth internalAuth = internalLoginController.findInternalAuthByEmailAndPassword(email, password);
    
    if (internalAuth != null) {
      UserEntity userEntity = userEntityController.findUserEntityById(internalAuth.getUserEntityId());
      if (userEntity != null) {
        User user = userController.findUserByUserEntity(userEntity);
        if (user != null) {
          return processLogin(authSource, requestParameters, DigestUtils.md5Hex("INTERNAL-" + internalAuth.getId()), Arrays.asList(email), user.getFirstName(), user.getLastName());
        }
      }
    }
    
    return new AuthenticationResult(Status.INVALID_CREDENTIALS);
  }

  @Override
  public String getDescription() {
    return "Muikku account";
  }

}