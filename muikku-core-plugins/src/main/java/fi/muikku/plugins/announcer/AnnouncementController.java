package fi.muikku.plugins.announcer;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import fi.muikku.model.users.UserEntity;
import fi.muikku.plugins.announcer.dao.AnnouncementDAO;
import fi.muikku.plugins.announcer.model.Announcement;

public class AnnouncementController {
  
  @Inject
  private AnnouncementDAO announcementDAO;
  
  public Announcement create(
      UserEntity publisher,
      String caption,
      String content,
      Date startDate,
      Date endDate
  ) {
    return announcementDAO.create(
        publisher.getId(),
        caption,
        content,
        new Date(),
        startDate,
        endDate,
        false
    );
  }

  public Announcement update(
      Announcement announcement,
      String caption,
      String content,
      Date startDate,
      Date endDate
  ) {
    return announcementDAO.update(
        announcement,
        caption,
        content,
        startDate,
        endDate
    );
  }
  
  public List<Announcement> listUnarchived() {
    return announcementDAO.listByArchived(false);
  }
  
  public List<Announcement> listAll() {
    return announcementDAO.listAll();
  }
  
  public Announcement findById(Long id) {
    return announcementDAO.findById(id);
  }
  
  public void archive(Announcement announcement) {
    announcementDAO.archive(announcement);
  }
}
