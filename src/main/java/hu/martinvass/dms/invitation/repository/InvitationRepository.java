package hu.martinvass.dms.invitation.repository;

import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long>, JpaSpecificationExecutor<Invitation> {

    List<Invitation> findByCorporationId(Long corporationId);
    List<Invitation> findByCorporationIdAndStatus(Long corporationId, InvitationStatus status);
    Optional<Invitation> findByCode(String code);

    Page<Invitation> findByCorporationId(Long corporationId, Pageable pageable);

    long countByCorporationIdAndStatus(Long corporationId, InvitationStatus status);

    List<Invitation> findAllByStatusAndExpiresAtBefore(InvitationStatus status, Date expiresAt);
}