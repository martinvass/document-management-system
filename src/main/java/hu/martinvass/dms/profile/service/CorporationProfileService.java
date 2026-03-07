package hu.martinvass.dms.profile.service;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.repository.AppUserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorporationProfileService {

    private final AppUserRepository userRepository;
    private final CorporationProfileRepository profileRepository;
    private final ProfileSessionService activeSession;

    @Transactional(readOnly = true)
    public void switchActiveProfile(Long userProfileId, String username, HttpSession session) {
        var appUser = userRepository.findByProfile_Username(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        var toActivate = profileRepository.findByIdAndUser_Profile_Username(userProfileId, username)
                .orElseThrow(() -> new RuntimeException("Profile ID " + userProfileId + " is not available for user " + username));

        appUser.setActiveProfile(toActivate);

        // Set in session
        activeSession.setActiveProfile(session, toActivate.getId());
    }

    public Page<CorporationProfile> findFiltered(
            Corporation corporation,
            String role,
            Long departmentId,
            Pageable pageable
    ) {
        Specification<CorporationProfile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("corporation"), corporation));

            if (role != null && !role.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), CorporationRole.valueOf(role)));
            }

            if (departmentId != null) {
                Join<Object, Object> departmentsJoin = root.join("departments", JoinType.INNER);
                predicates.add(cb.equal(departmentsJoin.get("id"), departmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return profileRepository.findAll(spec, pageable);
    }
}