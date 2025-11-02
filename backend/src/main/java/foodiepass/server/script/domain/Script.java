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
        validate(travelerScript, ScriptErrorCode.INVALID_TRAVELER_SCRIPT);
        validate(localScript, ScriptErrorCode.INVALID_LOCAL_SCRIPT);
        this.travelerScript = travelerScript;
        this.localScript = localScript;
    }

    private void validate(String scriptText, ScriptErrorCode errorCode) {
        if (!StringUtils.hasText(scriptText)) {
            throw new ScriptException(errorCode);
        }
    }
}
