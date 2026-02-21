package hu.martinvass.dms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private Long profileId;
    private String firstName;
    private String lastName;
    private String email;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}