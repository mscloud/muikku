package fi.muikku.rest.wall;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import fi.muikku.rest.AbstractRESTService;
import fi.muikku.controller.ForumController;
import fi.muikku.controller.UserController;
import fi.muikku.controller.WallController;
import fi.muikku.model.forum.ForumThread;
import fi.muikku.model.forum.ForumThreadReply;
import fi.muikku.model.stub.users.UserEntity;
import fi.muikku.model.wall.Wall;
import fi.muikku.model.wall.WallEntry;
import fi.muikku.model.wall.WallEntryReply;
import fi.muikku.model.wall.WallEntryVisibility;
import fi.muikku.schooldata.entity.User;
import fi.muikku.schooldata.local.wall.UserFeedItem;
import fi.muikku.security.AuthorizationException;
import fi.muikku.security.LoggedIn;
import fi.muikku.session.SessionController;
import fi.tranquil.TranquilModelEntity;
import fi.tranquil.TranquilModelType;
import fi.tranquil.Tranquility;
import fi.tranquil.TranquilityBuilder;
import fi.tranquil.TranquilityBuilderFactory;
import fi.tranquil.TranquilizingContext;
import fi.tranquil.instructions.PropertyInjectInstruction.ValueGetter;

@Path("/wall")
@Stateless
@Produces ("application/json")
public class WallRESTService extends AbstractRESTService {

  @Inject 
  private TranquilityBuilderFactory tranquilityBuilderFactory;

  @Inject
  private SessionController sessionController;
  
  @Inject
  private UserController userController;
  
  @Inject
  private WallController wallController;

  @Inject
  private ForumController forumController;
  
  @GET
  @Path ("/listUserFeedItems")
  public Response listUserFeedItems( 
      @QueryParam("userId") Long userId) {
    UserEntity user = userController.findUserEntity(userId); 
    
    List<UserFeedItem> userFeedItems = wallController.listUserFeedItems(user);

    TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
    Tranquility tranquility = tranquilityBuilder.createTranquility()
      .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction("wallEntry", tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction("wallEntry.replies", tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction("thread", tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction(ForumThread.class, tranquilityBuilder.createPropertyInjectInstruction("replies", new ForumThreadReplyInjector()))
      .addInstruction(Wall.class, tranquilityBuilder.createPropertyInjectInstruction("wallName", new WallEntityNameGetter()))
      .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()))
      .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("fullName", new UserNameValueGetter()));
    
    Collection<TranquilModelEntity> entities = tranquility.entities(userFeedItems);
    
    return Response.ok(
      entities
    ).build();
  }
  
  @GET
  @Path ("/{WALLID}/listWallEntries")
  public Response listWallEntries( 
      @PathParam ("WALLID") Long wallId) {
    
    Wall wall = wallController.findWallById(wallId); 

    List<WallEntry> entries = wallController.listWallEntries(wall);
    
    TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
    Tranquility tranquility = tranquilityBuilder.createTranquility()
      .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction("replies", tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
      .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()));
    
    Collection<TranquilModelEntity> entities = tranquility.entities(entries);
    
    return Response.ok(
      entities
    ).build();
  }

  @POST
  @Path ("/{WALLID}/addTextEntry") 
  @LoggedIn
  public Response addTextEntry(
      @PathParam ("WALLID") Long wallId,
      @FormParam ("text") String text,
      @FormParam ("visibility") String visibility
   ) throws AuthorizationException {
    UserEntity user = sessionController.getUser();

    Wall wall = wallController.findWallById(wallId);

    if (!wallController.canPostEntry(wall))
      throw new AuthorizationException("Not authorized");

    try {
      WallEntry entry = wallController.createWallEntry(wall, WallEntryVisibility.valueOf(visibility), user);

      wallController.createWallEntryTextItem(entry, text, user);
      
      TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
      Tranquility tranquility = tranquilityBuilder.createTranquility()
        .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
        .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()));
      
      return Response.ok(
        tranquility.entity(entry)
      ).build();
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }

  @POST
  @Path ("/{WALLID}/addGuidanceRequest") 
  @LoggedIn
  public Response addGuidanceRequest(
      @PathParam ("WALLID") Long wallId,
      @FormParam ("text") String text,
      @FormParam ("visibility") String visibility
   ) throws AuthorizationException {
    UserEntity user = sessionController.getUser();

    Wall wall = wallController.findWallById(wallId);

    if (!wallController.canPostEntry(wall))
      throw new AuthorizationException("Not authorized");

    try {
      WallEntry entry = wallController.createWallEntry(wall, WallEntryVisibility.valueOf(visibility), user);
      
      wallController.createWallEntryGuidanceRequestItem(entry, text, user);
      
      TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
      Tranquility tranquility = tranquilityBuilder.createTranquility()
        .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
        .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()));

      return Response.ok(
        tranquility.entity(entry)
      ).build();
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }


  @POST
  @Path ("/{WALLID}/addWallEntryComment") 
  @LoggedIn
  public Response addWallEntryComment(
      @PathParam ("WALLID") Long wallId,
      @FormParam ("wallEntryId") Long wallEntryId,
      @FormParam ("text") String text
   ) throws AuthorizationException {
    UserEntity user = sessionController.getUser();

    Wall wall = wallController.findWallById(wallId);

    // TODO: oikeudet entryyn
    
    if (!wallController.canPostEntry(wall))
      throw new AuthorizationException("Not authorized");

    WallEntry wallEntry = wallController.findWallEntryById(wallEntryId);
    
    try {
      WallEntryReply reply = wallController.createWallEntryReply(wall, wallEntry, user);
      wallController.createWallEntryTextItem(reply, text, user);
      
      TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
      Tranquility tranquility = tranquilityBuilder.createTranquility()
        .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
        .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()));

      return Response.ok(
          tranquility.entity(reply)
        ).build();
      
    } catch (ConstraintViolationException violationException) {
      return getConstraintViolations(violationException);
    }
  }
  
  private class UserEntityHasPictureValueGetter implements ValueGetter<Boolean> {
    @Override
    public Boolean getValue(TranquilizingContext context) {
      UserEntity user = (UserEntity) context.getEntityValue();
      return userController.getUserHasPicture(user);
    }
  }

  private class UserNameValueGetter implements ValueGetter<String> {
    @Override
    public String getValue(TranquilizingContext context) {
      UserEntity userEntity = (UserEntity) context.getEntityValue();
      User user = userController.findUser(userEntity);
      return user.getFirstName() + " " + user.getLastName();
    }
  }

  private class WallEntityNameGetter implements ValueGetter<String> {
    @Override
    public String getValue(TranquilizingContext context) {
      Wall wall = (Wall) context.getEntityValue();
      return wallController.getWallName(wall);
    }
  }

  private class ForumThreadReplyInjector implements ValueGetter<Collection<TranquilModelEntity>> {
    @Override
    public Collection<TranquilModelEntity> getValue(TranquilizingContext context) {
      ForumThread forumThread = (ForumThread) context.getEntityValue();
      
      List<ForumThreadReply> replies = forumController.listForumThreadReplies(forumThread);
      TranquilityBuilder tranquilityBuilder = tranquilityBuilderFactory.createBuilder();
      Tranquility tranquility = tranquilityBuilder.createTranquility()
          .addInstruction(tranquilityBuilder.createPropertyTypeInstruction(TranquilModelType.COMPLETE))
          .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("hasPicture", new UserEntityHasPictureValueGetter()))
          .addInstruction(UserEntity.class, tranquilityBuilder.createPropertyInjectInstruction("fullName", new UserNameValueGetter()));
      
      return tranquility.entities(replies);
    }
  }
}
