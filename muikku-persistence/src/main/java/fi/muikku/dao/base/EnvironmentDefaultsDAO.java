package fi.muikku.dao.base;

import fi.muikku.dao.CoreDAO;
import fi.muikku.dao.DAO;
import fi.muikku.model.base.Environment;
import fi.muikku.model.base.EnvironmentDefaults;
import fi.muikku.model.base.EnvironmentDefaults_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@DAO
public class EnvironmentDefaultsDAO extends CoreDAO<EnvironmentDefaults> {

	private static final long serialVersionUID = -3385964084597820036L;

	public EnvironmentDefaults findByEnvironment(Environment environment) {
    EntityManager entityManager = getEntityManager(); 
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EnvironmentDefaults> criteria = criteriaBuilder.createQuery(EnvironmentDefaults.class);
    Root<EnvironmentDefaults> root = criteria.from(EnvironmentDefaults.class);
    criteria.select(root);
    criteria.where(
        criteriaBuilder.equal(root.get(EnvironmentDefaults_.environment), environment)
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
}
