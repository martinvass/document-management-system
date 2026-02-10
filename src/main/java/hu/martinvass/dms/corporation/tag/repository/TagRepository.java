package hu.martinvass.dms.corporation.tag.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find all tags for a corporation
     */
    List<Tag> findByCorporationOrderByNameAsc(Corporation corporation);

    /**
     * Find a tag by name (normalized) and corporation
     */
    Optional<Tag> findByCorporationAndName(Corporation corporation, String name);

    /**
     * Check if a tag exists for a corporation
     */
    boolean existsByCorporationAndName(Corporation corporation, String name);
}