package foodiepass.server.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for submitting a survey response.
 *
 * <p>Captures user's confidence level after viewing the menu scan results.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRequest {

    /**
     * ID of the menu scan this survey response is for.
     */
    @NotNull(message = "scanId cannot be null")
    private UUID scanId;

    /**
     * User's confidence response.
     * - true: "Yes, I feel confident to order"
     * - false: "No, I still feel uncertain"
     */
    @NotNull(message = "hasConfidence cannot be null")
    private Boolean hasConfidence;
}
