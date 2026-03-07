package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.dto.UserSearchDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/corporation/users")
@AllArgsConstructor
public class UserSearchController {

    private final CorporationProfileRepository profileRepository;

    /**
     * Search users for permission granting (autocomplete)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(
            @ActiveUserProfile CorporationProfile activeProfile,
            @RequestParam String query
    ) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        var searchTerm = query.toLowerCase().trim();

        var results = profileRepository.findByCorporation(activeProfile.getCorporation())
                .stream()
                .filter(p -> !p.getId().equals(activeProfile.getId())) // Exclude current user
                .filter(p -> {
                    var fullName = (p.getUser().getProfile().getFirstName() + " " +
                            p.getUser().getProfile().getLastName()).toLowerCase();
                    var email = p.getUser().getProfile().getEmail().toLowerCase();

                    return fullName.contains(searchTerm) || email.contains(searchTerm);
                })
                .limit(10) // Max 10 results
                .map(p -> new UserSearchDto(
                        p.getId(),
                        p.getUser().getProfile().getFirstName(),
                        p.getUser().getProfile().getLastName(),
                        p.getUser().getProfile().getEmail()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }
}