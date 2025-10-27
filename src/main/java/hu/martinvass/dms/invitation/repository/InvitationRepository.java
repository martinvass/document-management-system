package hu.martinvass.dms.invitation.repository;

import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByCorporationId(Long corporationId);
    List<Invitation> findByCorporationIdAndStatus(Long corporationId, InvitationStatus status);
    Optional<Invitation> findByCode(String code);

    long countByCorporationIdAndStatus(Long corporationId, InvitationStatus status);
}