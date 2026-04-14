package sg.edu.nus.iss.email.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.edu.nus.iss.email.entity.EmailLog;

import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    Page<EmailLog> findByWorkflowId(String workflowId, Pageable pageable);

    Page<EmailLog> findByRecipientEmail(String recipientEmail, Pageable pageable);

    Page<EmailLog> findByStatus(EmailLog.Status status, Pageable pageable);

    boolean existsByWorkflowIdAndRecipientEmailAndEmailType(
            String workflowId, String recipientEmail, EmailLog.EmailType emailType);
}
