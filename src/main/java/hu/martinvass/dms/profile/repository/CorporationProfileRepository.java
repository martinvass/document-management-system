package hu.martinvass.dms.profile.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.profile.CorporationProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<CorporationProfile> findByCorporation(Corporation corporationId);
    Page<CorporationProfile> findByCorporation(Corporation corporation, Pageable pageable);
    List<CorporationProfile> findByDepartmentsContaining(Department department);
    long countByCorporation(Corporation corporation);
}