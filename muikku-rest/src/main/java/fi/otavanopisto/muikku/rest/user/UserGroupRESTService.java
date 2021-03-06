package fi.otavanopisto.muikku.rest.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.muikku.model.users.UserEntity;
import fi.otavanopisto.muikku.model.users.UserGroupEntity;
import fi.otavanopisto.muikku.rest.AbstractRESTService;
import fi.otavanopisto.muikku.rest.RESTPermitUnimplemented;
import fi.otavanopisto.muikku.schooldata.SchoolDataIdentifier;
import fi.otavanopisto.muikku.schooldata.entity.UserGroup;
import fi.otavanopisto.muikku.search.SearchProvider;
import fi.otavanopisto.muikku.search.SearchResult;
import fi.otavanopisto.muikku.security.MuikkuPermissions;
import fi.otavanopisto.muikku.security.RoleFeatures;
import fi.otavanopisto.muikku.session.SessionController;
import fi.otavanopisto.muikku.users.UserEntityController;
import fi.otavanopisto.muikku.users.UserGroupController;
import fi.otavanopisto.muikku.users.UserGroupEntityController;

@Stateful
@RequestScoped
@Path("/usergroup")
@Produces("application/json")
public class UserGroupRESTService extends AbstractRESTService {

  @Inject
  private SessionController sessionController;

  @Inject
  private UserGroupEntityController userGroupEntityController;
  
  @Inject
  private UserEntityController userEntityController;

  @Inject
  private UserGroupController userGroupController;

  @Inject
  @Any
  private Instance<SearchProvider> searchProviders;

  @Inject
  private Logger logger;

  @GET
  @Path("/groups")
  @RESTPermitUnimplemented
  public Response searchUserGroups(
      @QueryParam("userIdentifier") String userIdentifier,
      @QueryParam("searchString") String searchString,
      @QueryParam("firstResult") @DefaultValue("0") Integer firstResult,
      @QueryParam("maxResults") @DefaultValue("10") Integer maxResults) {

    if (!sessionController.isLoggedIn()) {
      return Response.status(Status.FORBIDDEN).build();
    }

    List<UserGroupEntity> entities = new ArrayList<>();

    if (userIdentifier != null) {
      SchoolDataIdentifier identifier = SchoolDataIdentifier.fromId(userIdentifier);
      
      if (identifier == null) {
        Response.status(Status.BAD_REQUEST).entity("Malformed userIdentifier").build();
      }
      
      UserEntity loggedUserEntity = sessionController.getLoggedUserEntity();
      UserEntity userEntity = userEntityController.findUserEntityByUserIdentifier(identifier);      

      if (userEntity == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      // Check for group-user-only roles - no shared groups, no rights
      if (sessionController.hasEnvironmentPermission(RoleFeatures.ACCESS_ONLY_GROUP_STUDENTS) && !userGroupEntityController.haveSharedUserGroups(loggedUserEntity, userEntity)) {
        return Response.status(Status.FORBIDDEN).build();
      }
      
      if (!(loggedUserEntity.getId().equals(userEntity.getId()) || sessionController.hasEnvironmentPermission(MuikkuPermissions.LIST_USER_USERGROUPS))) {
        return Response.status(Status.FORBIDDEN).build();
      }
      
      if (identifier != null) {
        entities = userGroupEntityController.listUserGroupsByUserIdentifier(identifier);
        
        // For someone with the role feature the group entities are not necessarily accessible
        if (sessionController.hasEnvironmentPermission(RoleFeatures.ACCESS_ONLY_GROUP_STUDENTS)) {
          List<UserGroupEntity> guiderGroups = userGroupEntityController.listUserGroupsByUserEntity(loggedUserEntity);
          Set<Long> guiderGroupIds = guiderGroups.stream().map(UserGroupEntity::getId).collect(Collectors.toSet());
          entities = entities.stream().filter((UserGroupEntity uge) -> guiderGroupIds.contains(uge.getId())).collect(Collectors.toList());
        }
      }
    } else {
      SearchProvider elasticSearchProvider = getProvider("elastic-search");
      if (elasticSearchProvider != null) {
        String[] fields = new String[] { "name" };
        SearchResult result = null;

        if (StringUtils.isBlank(searchString)) {
          result = elasticSearchProvider.matchAllSearch(firstResult, maxResults, UserGroup.class);
        } else {
          result = elasticSearchProvider.search(searchString, fields, firstResult, maxResults, UserGroup.class);
        }

        List<Map<String, Object>> results = result.getResults();

        if (!results.isEmpty()) {
          for (Map<String, Object> o : results) {
            String[] id = ((String) o.get("id")).split("/", 2);

            UserGroupEntity userGroupEntity = userGroupEntityController.findUserGroupEntityByDataSourceAndIdentifier(
                id[1], id[0]);
            if (userGroupEntity != null) {
              entities.add(userGroupEntity);
            }
          }
        }
      }
    }

    if (entities.isEmpty()) {
      return Response.noContent().build();
    } else {
      List<fi.otavanopisto.muikku.rest.model.UserGroup> ret = new ArrayList<fi.otavanopisto.muikku.rest.model.UserGroup>();

      for (UserGroupEntity entity : entities) {
        Long userCount = userGroupEntityController.getGroupUserCount(entity);
        UserGroup group = userGroupController.findUserGroup(entity);
        if (group != null)
          ret.add(new fi.otavanopisto.muikku.rest.model.UserGroup(entity.getId(), group.getName(), userCount));
        else
          logger.log(Level.WARNING, "Group not found");
      }

      return Response.ok(ret).build();
    }
  }

  @GET
  @Path("/groups/{ID}")
  @RESTPermitUnimplemented
  public Response findById(@PathParam("ID") Long groupId) {

    if (!sessionController.isLoggedIn()) {
      return Response.status(Status.FORBIDDEN).build();
    }

    UserGroupEntity userGroupEntity = userGroupEntityController.findUserGroupEntityById(groupId);

    if (userGroupEntity == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    UserGroup userGroup = userGroupController.findUserGroup(userGroupEntity);
    if (userGroup == null) {
      logger.severe("UserGroupEntity without UserGroup");
      return Response.status(Status.NOT_FOUND).build();
    }

    Long userCount = userGroupEntityController.getGroupUserCount(userGroupEntity);

    return Response.ok(
        new fi.otavanopisto.muikku.rest.model.UserGroup(userGroupEntity.getId(), userGroup.getName(), userCount))
        .build();
  }

  @GET
  @Path("/groups/{ID}/users")
  public Response listGroupUsersByGroup(@PathParam("ID") Long groupId) {
    return null;
  }

  private SearchProvider getProvider(String name) {
    Iterator<SearchProvider> i = searchProviders.iterator();
    while (i.hasNext()) {
      SearchProvider provider = i.next();
      if (name.equals(provider.getName())) {
        return provider;
      }
    }
    return null;
  }

}
