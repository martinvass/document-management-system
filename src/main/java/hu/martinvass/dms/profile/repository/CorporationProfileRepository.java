package hu.martinvass.dms.profile.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.profile.CorporationProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface CorporationProfileRepository extends JpaRepository<CorporationProfile, Long>, JpaSpecificationExecutor<CorporationProfile> {

    Optional<CorporationProfile> findByIdAndUser_Username(Long id, String username);
    List<CorporationProfile> findByUserId(Long userId);
    List<CorporationProfile> findByCorporation(Corporation corporationId);
    Page<CorporationProfile> findByCorporation(Corporation corporation, Pageable pageable);
    List<CorporationProfile> findByDepartmentsContaining(Department department);
    long countByCorporation(Corporation corporation);

    @Query("SELECT COUNT(cp) FROM CorporationProfile cp WHERE cp.corporation = :corporation AND cp.role = :role")
    long countByCorporationAndRole(
            @Param("corporation") Corporation corporation,
            @Param("role") CorporationRole role
    );

    @Query("SELECT COUNT(cp) FROM CorporationProfile cp WHERE cp.corporation = :corporation AND cp.user.creationDate >= :startDate")
    long countByCorporationAndCreatedAfter(
            @Param("corporation") Corporation corporation,
            @Param("startDate") LocalDateTime startDate
    );

    /**
     * Get profiles created within a date range
     */
    @Query("SELECT cp.createdAt, cp.id " +
            "FROM CorporationProfile cp " +
            "WHERE cp.corporation.id = :corporationId " +
            "AND cp.createdAt >= :fromDate " +
            "ORDER BY cp.createdAt ASC")
    List<Object[]> getProfilesForGrowth(@Param("corporationId") Long corporationId,
                                        @Param("fromDate") LocalDateTime fromDate);

    /**
     * Count profiles created BEFORE a certain date
     */
    @Query("SELECT COUNT(cp) " +
            "FROM CorporationProfile cp " +
            "WHERE cp.corporation.id = :corporationId " +
            "AND cp.createdAt < :beforeDate")
    Long countProfilesBeforeDate(@Param("corporationId") Long corporationId,
                                 @Param("beforeDate") LocalDateTime beforeDate);
}