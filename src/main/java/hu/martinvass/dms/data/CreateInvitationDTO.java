package hu.martinvass.dms.data;

import hu.martinvass.dms.corporation.CorporationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CreateInvitationDTO {

    @NotNull
    @Email
    private String email;

    @NotNull
    private CorporationRole role;

    @NotNull
    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiresAt;
}