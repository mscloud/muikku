package fi.muikku.plugins.calendar.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class UserCalendar {
  
	public Long getId() {
		return id;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public Long getEnvironmentId() {
		return environmentId;
	}
	
	public void setEnvironmentId(Long environmentId) {
		this.environmentId = environmentId;
	}
	
	public Calendar getCalendar() {
		return calendar;
	}
	
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	
  @Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private Long id;
	  
  // ManyToOne UserEntity
  @Column (name = "environment_id")
	private Long environmentId;

  // ManyToOne UserEntity
  @Column (name = "user_id")
	private Long userId;
	
  @ManyToOne
  private Calendar calendar;
}