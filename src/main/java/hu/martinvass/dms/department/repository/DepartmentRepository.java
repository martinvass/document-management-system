package hu.martinvass.dms.department.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.department.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCorporationOrderByNameAsc(Corporation corporation);
    Page<Department> findByCorporation(Corporation corporation, Pageable pageable);
    Optional<Department> findByIdAndCorporation(Long id, Corporation corporation);
    long countByCorporation(Corporation corporation);

    @Query("SELECT d FROM Department d WHERE d.corporation = :corporation " +
            "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Department> searchByName(
            @Param("corporation") Corporation corporation,
            @Param("search") String search,
            Pageable pageable
    );
}