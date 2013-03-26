package fi.muikku.dao.wall;

import java.util.Date;

import fi.muikku.dao.CoreDAO;
import fi.muikku.dao.DAO;
import fi.muikku.model.stub.users.UserEntity;
import fi.muikku.model.wall.AbstractWallEntry;
import fi.muikku.model.wall.WallEntryTextItem;


@DAO
public class WallEntryTextItemDAO extends CoreDAO<WallEntryTextItem> {

	private static final long serialVersionUID = 942406315121344409L;

	public WallEntryTextItem create(AbstractWallEntry wallEntry, String text, UserEntity creator) {
    Date now = new Date();

    return create(wallEntry, text, creator,creator, now, now, Boolean.FALSE);
  }

  public WallEntryTextItem create(AbstractWallEntry wallEntry, String text, UserEntity creator, UserEntity lastModfier, Date created, Date lastModified, Boolean archived) {
    WallEntryTextItem item = new WallEntryTextItem();
    
    item.setText(text);
    item.setWallEntry(wallEntry);
    item.setCreated(created);
    item.setLastModified(lastModified);
    item.setCreator(creator);
    item.setLastModifier(lastModfier);
    item.setArchived(archived);
    
    getEntityManager().persist(item);
    
    wallEntry.addItem(item);
    getEntityManager().persist(wallEntry);
    
    return item;
  }
}
