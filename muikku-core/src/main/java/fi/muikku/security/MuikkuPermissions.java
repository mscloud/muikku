package fi.muikku.security;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fi.muikku.model.users.EnvironmentRoleArchetype;
import fi.muikku.model.workspace.WorkspaceRoleArchetype;


@ApplicationScoped
public class MuikkuPermissions extends AbstractMuikkuPermissionCollection implements MuikkuPermissionCollection {

  @Scope (PermissionScope.ENVIRONMENT)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  public static final String REPRESENT_USER = "REPRESENT_USER";
  
  @Scope (PermissionScope.ENVIRONMENT)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  public static final String CREATE_WORKSPACE = "CREATE_WORKSPACE";
  
  @Scope (PermissionScope.ENVIRONMENT)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER, EnvironmentRoleArchetype.STUDENT, EnvironmentRoleArchetype.TEACHER })
  public static final String LIST_WORKSPACES = "LIST_WORKSPACES";
  
  @Scope (PermissionScope.ENVIRONMENT)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  public static final String MANAGE_SETTINGS = "MANAGE_SETTINGS";
  
  @Scope (PermissionScope.ENVIRONMENT)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  public static final String MANAGE_USERS = "MANAGE_USERS";
  
  /* WORKSPACE */
  
  @Scope (PermissionScope.WORKSPACE)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER, EnvironmentRoleArchetype.STUDENT, EnvironmentRoleArchetype.TEACHER })
  public static final String JOIN_WORKSPACE = "JOIN_WORKSPACE";

  @Scope (PermissionScope.WORKSPACE)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  @DefaultWorkspacePermissionRoles({ WorkspaceRoleArchetype.TEACHER })
  public static final String WORKSPACE_MANAGEWORKSPACESETTINGS = "WORKSPACE_MANAGEWORKSPACESETTINGS";
  
  @Scope (PermissionScope.WORKSPACE)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  @DefaultWorkspacePermissionRoles({ WorkspaceRoleArchetype.TEACHER })
  public static final String REPRESENT_WORKSPACEUSER = "REPRESENT_WORKSPACEUSER";
  
  @Scope (PermissionScope.WORKSPACE)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  @DefaultWorkspacePermissionRoles({ WorkspaceRoleArchetype.TEACHER })
  public static final String EVALUATE_USER = "EVALUATE_USER";

  /* RESOURCE */
  
  @Scope (PermissionScope.RESOURCE)
  @DefaultEnvironmentPermissionRoles ({ EnvironmentRoleArchetype.MANAGER })
  public static final String MANAGE_RESOURCERIGHTS = "MANAGE_RESOURCERIGHTS";
  
  @Override
  public List<String> listPermissions() {
    return listPermissions(MuikkuPermissions.class);
  }

  @Override
  public boolean containsPermission(String permission) {
    return listPermissions().contains(permission);
  }
  
  @Override
  public String getPermissionScope(String permission) throws NoSuchFieldException {
    return getPermissionScope(MuikkuPermissions.class, permission);
  }

  @Override
  public String[] getDefaultPseudoRoles(String permission) throws NoSuchFieldException {
    return getDefaultPseudoRoles(MuikkuPermissions.class, permission);
  }

  @Override
  public EnvironmentRoleArchetype[] getDefaultEnvironmentRoles(String permission) throws NoSuchFieldException {
    return getDefaultEnvironmentRoles(MuikkuPermissions.class, permission);
  }

  @Override
  public WorkspaceRoleArchetype[] getDefaultWorkspaceRoles(String permission) throws NoSuchFieldException {
    return getDefaultWorkspaceRoles(MuikkuPermissions.class, permission);
  }
  
}
