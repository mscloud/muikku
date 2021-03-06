package fi.otavanopisto.muikku.plugins.evaluation.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table (
  uniqueConstraints = {
    @UniqueConstraint (columnNames = { "studentEntityId", "workspaceEntityId" }),
    @UniqueConstraint (columnNames = { "studentEntityId", "workspaceMaterialId" })
  }    
)
public class SupplementationRequest {

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public Long getUserEntityId() {
    return userEntityId;
  }

  public void setUserEntityId(Long userEntityId) {
    this.userEntityId = userEntityId;
  }

  public Long getStudentEntityId() {
    return studentEntityId;
  }

  public void setStudentEntityId(Long studentEntityId) {
    this.studentEntityId = studentEntityId;
  }

  public Long getWorkspaceEntityId() {
    return workspaceEntityId;
  }

  public void setWorkspaceEntityId(Long workspaceEntityId) {
    this.workspaceEntityId = workspaceEntityId;
  }

  public Long getWorkspaceMaterialId() {
    return workspaceMaterialId;
  }

  public void setWorkspaceMaterialId(Long workspaceMaterialId) {
    this.workspaceMaterialId = workspaceMaterialId;
  }

  public String getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    this.requestText = requestText;
  }

  public Boolean getArchived() {
    return archived;
  }

  public void setArchived(Boolean archived) {
    this.archived = archived;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(nullable = false)
  private Long userEntityId;

  @NotNull
  @Column(nullable = false)
  private Long studentEntityId;

  private Long workspaceEntityId;
  
  private Long workspaceMaterialId;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date requestDate;

  @Lob
  private String requestText;

  @NotNull
  @Column(nullable = false)
  private Boolean archived;
  
}
