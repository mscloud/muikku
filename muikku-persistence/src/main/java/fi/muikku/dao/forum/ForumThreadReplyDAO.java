package fi.muikku.dao.forum;

import java.util.Date;
import java.util.List;

import fi.muikku.dao.CoreDAO;
import fi.muikku.dao.DAO;
import fi.muikku.model.forum.ForumArea;
import fi.muikku.model.forum.ForumThread;
import fi.muikku.model.forum.ForumThreadReply;
import fi.muikku.model.forum.ForumThreadReply_;
import fi.muikku.model.forum.ForumThread_;
import fi.muikku.model.stub.users.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@DAO
public class ForumThreadReplyDAO extends CoreDAO<ForumThreadReply> {
  
	private static final long serialVersionUID = 6996591519523286352L;

	public ForumThreadReply create(ForumArea forumArea, ForumThread thread, String message, UserEntity creator) {
    Date now = new Date();

    return create(forumArea, thread, message, now, creator, now, creator, false);
  }
  
  public ForumThreadReply create(ForumArea forumArea, ForumThread thread, String message, Date created, UserEntity creator, Date lastModified, UserEntity lastModifier, Boolean archived) {
    ForumThreadReply reply = new ForumThreadReply();
    
    reply.setForumArea(forumArea);
    reply.setThread(thread);
    reply.setMessage(message);
    reply.setCreated(created);
    reply.setCreator(creator);
    reply.setLastModified(lastModified);
    reply.setLastModifier(lastModifier);
    reply.setArchived(archived);
    
    getEntityManager().persist(reply);
    
    return reply;
  }
  
  public List<ForumThreadReply> listByForumThread(ForumThread forumThread) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ForumThreadReply> criteria = criteriaBuilder.createQuery(ForumThreadReply.class);
    Root<ForumThreadReply> root = criteria.from(ForumThreadReply.class);
    criteria.select(root);
    criteria.where(
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get(ForumThreadReply_.thread), forumThread),
            criteriaBuilder.equal(root.get(ForumThread_.archived), Boolean.FALSE)
        )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
}
