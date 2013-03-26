package fi.muikku.model.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import fi.muikku.model.base.Environment;
import fi.muikku.model.stub.users.UserEntity;
import fi.muikku.model.util.ArchivableEntity;

@Entity
public class EnvironmentUser implements ArchivableEntity {

  /**
   * Returns internal unique id
   * 
   * @return Internal unique id
   */
  public Long getId() {
    return id;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public UserEntity getUser() {
    return user;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public EnvironmentUserRole getRole() {
    return role;
  }

  public void setRole(EnvironmentUserRole role) {
    this.role = role;
  }

  public void setArchived(Boolean archived) {
    this.archived = archived;
  }

  public Boolean getArchived() {
    return archived;
  }

  @Id
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne
  private Environment environment;
  
  @ManyToOne
  private UserEntity user;
  
  @ManyToOne
  private EnvironmentUserRole role;

  @NotNull
  @Column(nullable = false)
  private Boolean archived = Boolean.FALSE;
}
