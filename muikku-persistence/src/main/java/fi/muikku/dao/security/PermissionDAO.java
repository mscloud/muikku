package fi.muikku.dao.security;

import java.util.List;

import fi.muikku.dao.CoreDAO;
import fi.muikku.dao.DAO;
import fi.muikku.model.security.Permission;
import fi.muikku.model.security.Permission_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@DAO
public class PermissionDAO extends CoreDAO<Permission> {

	private static final long serialVersionUID = 1865691516876354591L;

	public Permission findByName(String name) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Permission> criteria = criteriaBuilder.createQuery(Permission.class);
    Root<Permission> root = criteria.from(Permission.class);
    criteria.select(root);
    criteria.where(
        criteriaBuilder.equal(root.get(Permission_.name), name)
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  public List<Permission> listByScope(String scope) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Permission> criteria = criteriaBuilder.createQuery(Permission.class);
    Root<Permission> root = criteria.from(Permission.class);
    criteria.select(root);
    criteria.where(
        criteriaBuilder.equal(root.get(Permission_.scope), scope)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
}
