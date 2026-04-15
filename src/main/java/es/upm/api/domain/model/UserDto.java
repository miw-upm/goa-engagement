package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String mobile;
    private String firstName;
    private String familyName;
    private String documentType;
    private String identity;

    public String toFullName() {
        return "D./Dña. " + firstName + " " + familyName;
    }

    public String toFullNameAndIdentity() {
        return this.toFullName() + " con " + formatDocumentType() + " nº " + this.identity;
    }

    private String formatDocumentType() {
        return String.join(".", documentType.toUpperCase().split("")) + ".";
    }

}
