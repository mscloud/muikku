package fi.muikku.plugins.material.coops;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.muikku.plugins.material.coops.dao.CoOpsSessionDAO;
import fi.muikku.plugins.material.coops.event.CoOpsSessionCloseEvent;
import fi.muikku.plugins.material.coops.event.CoOpsSessionOpenEvent;
import fi.muikku.plugins.material.coops.model.CoOpsSession;
import fi.muikku.plugins.material.coops.model.CoOpsSessionType;
import fi.muikku.plugins.material.model.HtmlMaterial;

@Dependent
@Stateless
public class CoOpsSessionController {
  
  private static final long SESSION_TIMEOUT = 10 * 1000;

  @Inject
  private Event<CoOpsSessionOpenEvent> sessionOpenEvent;
  
  @Inject
  private Event<CoOpsSessionCloseEvent> sessionCloseEvent;
  
  @Inject
  private CoOpsSessionDAO coOpsSessionDAO;
  
  public CoOpsSession createSession(HtmlMaterial htmlMaterial, String sessionId, Long joinRevision, String algorithm) {
    CoOpsSession session = coOpsSessionDAO.create(htmlMaterial, sessionId, CoOpsSessionType.REST, joinRevision, algorithm, Boolean.FALSE, new Date());
    sessionOpenEvent.fire(new CoOpsSessionOpenEvent(session.getSessionId()));
    return session;
  }

  public CoOpsSession findSessionBySessionId(String sessionId) {
    return coOpsSessionDAO.findBySessionId(sessionId);
  }
  
  public List<CoOpsSession> listTimedoutRestSessions() {
    Date accessedBefore = new Date(System.currentTimeMillis() - SESSION_TIMEOUT);
    return coOpsSessionDAO.listByAccessedBeforeAndTypeAndClosed(accessedBefore, CoOpsSessionType.REST, Boolean.FALSE);
  }

  public List<CoOpsSession> listSessionsByHtmlMaterialAndClosed(HtmlMaterial htmlMaterial, Boolean closed) {
    return coOpsSessionDAO.listByFileAndClosed(htmlMaterial, closed);
  }
  
  public CoOpsSession updateSessionType(CoOpsSession coOpsSession, CoOpsSessionType type) {
    return coOpsSessionDAO.updateType(coOpsSession, type);
  }

  public void closeSession(CoOpsSession session) {
    closeSession(session, false);
  }
  
  public void closeSession(CoOpsSession session, boolean quiet) {
    coOpsSessionDAO.updateClosed(session, Boolean.TRUE);
    if (!quiet) {
      sessionCloseEvent.fire(new CoOpsSessionCloseEvent(session.getSessionId()));
    }
  }
}