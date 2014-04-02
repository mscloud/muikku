package fi.muikku.plugins.communicator;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import fi.muikku.dao.users.UserEntityDAO;
import fi.muikku.model.base.Tag;
import fi.muikku.model.users.UserEntity;
import fi.muikku.notifier.NotifierController;
import fi.muikku.plugins.communicator.dao.CommunicatorMessageIdDAO;
import fi.muikku.plugins.communicator.dao.CommunicatorMessageRecipientDAO;
import fi.muikku.plugins.communicator.dao.CommunicatorMessageSignatureDAO;
import fi.muikku.plugins.communicator.dao.CommunicatorMessageTemplateDAO;
import fi.muikku.plugins.communicator.dao.InboxCommunicatorMessageDAO;
import fi.muikku.plugins.communicator.model.CommunicatorMessage;
import fi.muikku.plugins.communicator.model.CommunicatorMessageId;
import fi.muikku.plugins.communicator.model.CommunicatorMessageRecipient;
import fi.muikku.plugins.communicator.model.CommunicatorMessageSignature;
import fi.muikku.plugins.communicator.model.CommunicatorMessageTemplate;
import fi.muikku.plugins.communicator.model.InboxCommunicatorMessage;
import fi.muikku.security.Permit;
import fi.muikku.security.PermitContext;
import fi.muikku.session.SessionController;

@Dependent
@Stateful
@Named("Communicator")
public class CommunicatorController {
  @Inject
  private UserEntityDAO userDAO;

  @Inject
  private SessionController sessionController;

  @Inject
  private fi.muikku.schooldata.UserController userController;
  
  @Inject
  private InboxCommunicatorMessageDAO communicatorMessageDAO;
  
  @Inject
  private CommunicatorMessageRecipientDAO communicatorMessageRecipientDAO;

  @Inject
  private CommunicatorMessageIdDAO communicatorMessageIdDAO;

  @Inject
  private CommunicatorMessageTemplateDAO communicatorMessageTemplateDAO;
  
  @Inject
  private CommunicatorMessageSignatureDAO communicatorMessageSignatureDAO;
  
  @Inject
  private NotifierController notifierController;
  
  /**
   * 
   * @param user
   * @return
   */

  public List<InboxCommunicatorMessage> listReceivedItems(UserEntity userEntity) {
    return communicatorMessageDAO.listFirstMessagesByRecipient(userEntity);
  }
  
  public List<InboxCommunicatorMessage> listSentItems(UserEntity userEntity) {
    return communicatorMessageDAO.listFirstMessagesBySender(userEntity);
  }
  
  public CommunicatorMessageId createMessageId() {
    return communicatorMessageIdDAO.create();
  }
  
  public CommunicatorMessage createMessage(CommunicatorMessageId communicatorMessageId, UserEntity sender, List<UserEntity> recipients, 
      String caption, String content, Set<Tag> tags) {
    CommunicatorMessage message = communicatorMessageDAO.create(communicatorMessageId, sender.getId(), caption, content, new Date(), tags);
    
    for (UserEntity recipient : recipients) {
      communicatorMessageRecipientDAO.create(message, recipient.getId());
    }
    
    return message;
  }

  public CommunicatorMessageId findCommunicatorMessageId(Long communicatorMessageId) {
    return communicatorMessageIdDAO.findById(communicatorMessageId);
  }

  public List<InboxCommunicatorMessage> listInboxMessagesByMessageId(UserEntity user, CommunicatorMessageId messageId) {
    return communicatorMessageDAO.listMessagesByRecipientAndMessageId(user, messageId);
  }

  public CommunicatorMessage findCommunicatorMessageById(Long communicatorMessageId) {
    return communicatorMessageDAO.findById(communicatorMessageId);
  }
  
  public List<CommunicatorMessageRecipient> listCommunicatorMessageRecipients(CommunicatorMessage communicatorMessage) {
    return communicatorMessageRecipientDAO.listByMessage(communicatorMessage);
  }

  public Long countMessagesByRecipientAndMessageId(UserEntity recipient, CommunicatorMessageId communicatorMessageId) {
    return communicatorMessageDAO.countMessagesByRecipientAndMessageId(recipient, communicatorMessageId);
  }
  
  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public List<CommunicatorMessageTemplate> listMessageTemplates(@PermitContext UserEntity user) {
    return communicatorMessageTemplateDAO.listByUser(user);
  }
  
  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public List<CommunicatorMessageSignature> listMessageSignatures(@PermitContext UserEntity user) {
    return communicatorMessageSignatureDAO.listByUser(user);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageTemplate getMessageTemplate(Long id) {
    return communicatorMessageTemplateDAO.findById(id);
  }
  
  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageSignature getMessageSignature(Long id) {
    return communicatorMessageSignatureDAO.findById(id);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public void deleteMessageTemplate(@PermitContext CommunicatorMessageTemplate messageTemplate) {
    communicatorMessageTemplateDAO.delete(messageTemplate);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public void deleteMessageSignature(@PermitContext CommunicatorMessageSignature messageSignature) {
    communicatorMessageSignatureDAO.delete(messageSignature);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageTemplate editMessageTemplate(@PermitContext CommunicatorMessageTemplate messageTemplate, String name, String content) {
    return communicatorMessageTemplateDAO.update(messageTemplate, name, content);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageSignature editMessageSignature(@PermitContext CommunicatorMessageSignature messageSignature, String name, String signature) {
    return communicatorMessageSignatureDAO.update(messageSignature, name, signature);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageSignature createMessageSignature(String name, String content, @PermitContext UserEntity user) {
    return communicatorMessageSignatureDAO.create(name, content, user);
  }

  @Permit (CommunicatorPermissionCollection.COMMUNICATOR_MANAGE_SETTINGS)
  public CommunicatorMessageTemplate createMessageTemplate(String name, String content, @PermitContext UserEntity user) {
    return communicatorMessageTemplateDAO.create(name, content, user);
  }

  public void archiveMessage(UserEntity user, CommunicatorMessageId messageId) {
    List<CommunicatorMessageRecipient> received = communicatorMessageRecipientDAO.listByUserAndMessageId(user, messageId);
    
    for (CommunicatorMessageRecipient recipient : received) {
      communicatorMessageRecipientDAO.archiveRecipient(recipient);
    }
    
    List<InboxCommunicatorMessage> sent = communicatorMessageDAO.listBySenderAndMessageId(user, messageId);

    for (CommunicatorMessage msg : sent) {
      communicatorMessageDAO.archiveSent(msg);
    }
  }

  /**
   * List all messages with id user has sent or received.
   * 
   * @param user
   * @param messageId
   * @return
   */
  public List<InboxCommunicatorMessage> listMessagesByMessageId(UserEntity user, CommunicatorMessageId messageId) {
    return communicatorMessageDAO.listByMessageId(user, messageId);
  }
}
