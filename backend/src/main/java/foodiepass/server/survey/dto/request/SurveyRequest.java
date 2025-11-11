package foodiepass.server.survey.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for submitting a survey response.
 *
 * <p>Captures user's confidence level after viewing the menu scan results.
 *
 * @param scanId ID of the menu scan this survey response is for
 * @param hasConfidence User's confidence response (true: confident to order, false: still uncertain)
 */
public record SurveyRequest(
    @NotNull(message = "scanId cannot be null")
    UUID scanId,

    @NotNull(message = "hasConfidence cannot be null")
    Boolean hasConfidence
) {}
