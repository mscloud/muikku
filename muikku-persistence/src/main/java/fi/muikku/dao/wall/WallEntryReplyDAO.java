package fi.muikku.dao.wall;

import java.util.Date;
import java.util.List;

import fi.muikku.dao.CoreDAO;
import fi.muikku.dao.DAO;
import fi.muikku.model.stub.users.UserEntity;
import fi.muikku.model.wall.Wall;
import fi.muikku.model.wall.WallEntry;
import fi.muikku.model.wall.WallEntryReply;
import fi.muikku.model.wall.WallEntryReply_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@DAO
public class WallEntryReplyDAO extends CoreDAO<WallEntryReply> {

	private static final long serialVersionUID = 4197097795887500969L;

	public WallEntryReply create(Wall wall, WallEntry wallEntry, UserEntity creator) {
    WallEntryReply comment = new WallEntryReply();
    
    Date now = new Date();

    comment.setWallEntry(wallEntry);
    comment.setCreated(now);
    comment.setLastModified(now);
    comment.setCreator(creator);
    comment.setLastModifier(creator);
    
    getEntityManager().persist(comment);
    
    return comment;
  }

  public List<WallEntryReply> listByWallEntry(WallEntry wallEntry) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<WallEntryReply> criteria = criteriaBuilder.createQuery(WallEntryReply.class);
    Root<WallEntryReply> root = criteria.from(WallEntryReply.class);
    criteria.select(root);
    criteria.where(
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get(WallEntryReply_.wallEntry), wallEntry),
            criteriaBuilder.equal(root.get(WallEntryReply_.archived), Boolean.FALSE)
        )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  public Date findMaxDateByWallEntry(WallEntry wallEntry) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Date> criteria = criteriaBuilder.createQuery(Date.class);
    Root<WallEntryReply> root = criteria.from(WallEntryReply.class);
    criteria.select(criteriaBuilder.greatest(root.get(WallEntryReply_.created)));
    criteria.where(
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get(WallEntryReply_.wallEntry), wallEntry),
            criteriaBuilder.equal(root.get(WallEntryReply_.archived), Boolean.FALSE)
        )
    );
    
    return entityManager.createQuery(criteria).getSingleResult();
  }
}
