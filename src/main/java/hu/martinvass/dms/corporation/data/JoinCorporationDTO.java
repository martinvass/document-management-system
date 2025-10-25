package hu.martinvass.dms.corporation.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinCorporationDTO {

    @NotBlank
    private String code;
}