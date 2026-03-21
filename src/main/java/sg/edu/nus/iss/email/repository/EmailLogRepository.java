package sg.edu.nus.iss.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.edu.nus.iss.email.entity.EmailLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    List<EmailLog> findByWorkflowId(String workflowId);

    List<EmailLog> findByRecipientEmail(String recipientEmail);

    List<EmailLog> findByStatus(EmailLog.Status status);
}
