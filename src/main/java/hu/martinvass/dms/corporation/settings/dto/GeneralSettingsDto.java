package hu.martinvass.dms.corporation.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralSettingsDto {

    private String name;
    private String description;
}