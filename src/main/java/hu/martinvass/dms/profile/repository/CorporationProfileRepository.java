package hu.martinvass.dms.profile.repository;

import hu.martinvass.dms.profile.CorporationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface CorporationProfileRepository extends JpaRepository<CorporationProfile, Long> {

    Optional<CorporationProfile> findByIdAndUser_Profile_Username(Long id, String username);

    List<CorporationProfile> findByUserId(Long userId);
    List<CorporationProfile> findByProfileId(Long profileId);
}