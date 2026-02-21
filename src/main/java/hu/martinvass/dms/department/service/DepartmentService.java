package hu.martinvass.dms.department.service;

import hu.martinvass.dms.activity.ActivityService;
import hu.martinvass.dms.activity.ActivityType;
import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.department.repository.DepartmentRepository;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CorporationProfileRepository profileRepository;

    private final ActivityService activityService;

    /**
     * Create a new department
     */
    @Transactional
    public Department createDepartment(
            Corporation corporation,
            String name,
            String description,
            AppUser createdBy
    ) {
        Department department = Department.builder()
                .corporation(corporation)
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .build();

        activityService.log(
                corporation,
                createdBy,
                ActivityType.DEPARTMENT_CREATED,
                department.getName(),
                department.getId()
        );

        return departmentRepository.save(department);
    }

    /**
     * Get all departments in a corporation
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments(Corporation corporation) {
        return departmentRepository.findByCorporationOrderByNameAsc(corporation);
    }

    /**
     * Get departments (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Department> getDepartments(Corporation corporation, Pageable pageable) {
        return departmentRepository.findByCorporation(corporation, pageable);
    }

    /**
     * Get a single department
     */
    @Transactional(readOnly = true)
    public Department getDepartment(Long id, Corporation corporation) {
        return departmentRepository.findByIdAndCorporation(id, corporation)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
    }

    /**
     * Update department
     */
    @Transactional
    public Department updateDepartment(
            Long id,
            Corporation corporation,
            String name,
            String description
    ) {
        Department department = getDepartment(id, corporation);
        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    /**
     * Add member to department
     */
    @Transactional
    public void addMember(Long departmentId, Long profileId, CorporationProfile requester) {
        // Check admin permission
        if (requester.getRole() != CorporationRole.ADMIN) {
            throw new SecurityException("Only admins can add members to departments");
        }

        Department department = getDepartment(departmentId, requester.getCorporation());
        CorporationProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + profileId));

        // Verify same corporation
        if (!profile.getCorporation().getId().equals(department.getCorporation().getId())) {
            throw new SecurityException("Profile and department must be in the same corporation");
        }

        profile.getDepartments().add(department);
        profileRepository.save(profile);

        activityService.log(
                requester.getCorporation(),
                requester.getUser(),
                ActivityType.USER_ADDED_TO_DEPT,
                department.getName() + " - " + profile.getUser().getProfile().getUsername(),
                department.getId()
        );
    }

    /**
     * Remove member from department
     */
    @Transactional
    public void removeMember(Long departmentId, Long profileId, CorporationProfile requester) {
        // Check admin permission
        if (requester.getRole() != CorporationRole.ADMIN) {
            throw new SecurityException("Only admins can remove members from departments");
        }

        Department department = getDepartment(departmentId, requester.getCorporation());
        CorporationProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + profileId));

        profile.getDepartments().remove(department);
        profileRepository.save(profile);

        activityService.log(
                requester.getCorporation(),
                requester.getUser(),
                ActivityType.USER_REMOVED_FROM_DEPT,
                department.getName() + " - " + profile.getUser().getProfile().getUsername(),
                department.getId()
        );
    }

    /**
     * Get members of a department
     */
    @Transactional(readOnly = true)
    public List<CorporationProfile> getDepartmentMembers(Long departmentId, Corporation corporation) {
        Department department = getDepartment(departmentId, corporation);

        return profileRepository.findByDepartmentsContaining(department);
    }

    /**
     * Count departments in corporation
     */
    @Transactional(readOnly = true)
    public long countDepartments(Corporation corporation) {
        return departmentRepository.countByCorporation(corporation);
    }
}