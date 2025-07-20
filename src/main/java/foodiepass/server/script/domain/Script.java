package foodiepass.server.script.domain;

import foodiepass.server.script.exception.ScriptErrorCode;
import foodiepass.server.script.exception.ScriptException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@EqualsAndHashCode
public final class Script {

    private final String travelerScript;
    private final String localScript;

    public Script(String travelerScript, String localScript) {
        validateTravelerScript(travelerScript);
        validateLocalScript(localScript);
        this.travelerScript = travelerScript;
        this.localScript = localScript;
    }

    private void validateTravelerScript(String travelerScript) {
        if (!StringUtils.hasText(travelerScript)) {
            throw new ScriptException(ScriptErrorCode.INVALID_TRAVELER_SCRIPT);
        }
    }

    private void validateLocalScript(String localScript) {
        if (!StringUtils.hasText(localScript)) {
            throw new ScriptException(ScriptErrorCode.INVALID_LOCAL_SCRIPT);
        }
    }
}
