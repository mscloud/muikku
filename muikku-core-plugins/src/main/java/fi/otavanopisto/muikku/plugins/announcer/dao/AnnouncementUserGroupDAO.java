package fi.otavanopisto.muikku.plugins.announcer.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.muikku.plugins.announcer.model.AnnouncementUserGroup_;
import fi.otavanopisto.muikku.plugins.CorePluginsDAO;
import fi.otavanopisto.muikku.plugins.announcer.model.Announcement;
import fi.otavanopisto.muikku.plugins.announcer.model.AnnouncementUserGroup;

public class AnnouncementUserGroupDAO extends CorePluginsDAO<AnnouncementUserGroup> {
	
  private static final long serialVersionUID = -6367885147690217019L;

  public AnnouncementUserGroup create(Announcement announcement, Long userGroupEntityId, Boolean archived) {
    AnnouncementUserGroup announcementUserGroup = new AnnouncementUserGroup();
    announcementUserGroup.setAnnouncement(announcement);
    announcementUserGroup.setUserGroupEntityId(userGroupEntityId);
    announcementUserGroup.setArchived(archived);
    
    return persist(announcementUserGroup);
 }
  
  public List<AnnouncementUserGroup> listByArchived(Boolean archived){
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AnnouncementUserGroup> criteria = criteriaBuilder.createQuery(AnnouncementUserGroup.class);
    Root<AnnouncementUserGroup> root = criteria.from(AnnouncementUserGroup.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(AnnouncementUserGroup_.archived), archived));
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  public List<AnnouncementUserGroup> listByAnnouncementAndArchived(Announcement announcement, Boolean archived) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AnnouncementUserGroup> criteria = criteriaBuilder.createQuery(AnnouncementUserGroup.class);
    Root<AnnouncementUserGroup> root = criteria.from(AnnouncementUserGroup.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(AnnouncementUserGroup_.announcement), announcement),
        criteriaBuilder.equal(root.get(AnnouncementUserGroup_.archived), archived)
      )
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  public AnnouncementUserGroup updateArchived(AnnouncementUserGroup announcementUserGroup, Boolean archived) {
    announcementUserGroup.setArchived(archived);
    return persist(announcementUserGroup);
  }
  
  public void delete(AnnouncementUserGroup announcementUserGroup){
    super.delete(announcementUserGroup);
  }
  
}
