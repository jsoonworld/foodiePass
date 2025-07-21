package foodiepass.server.script.api;

import foodiepass.server.script.application.ScriptService;
import foodiepass.server.script.dto.request.ScriptGenerateRequest;
import foodiepass.server.script.dto.response.ScriptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @PostMapping("/scripts/generate")
    public ScriptResponse generateScript(
            @RequestBody final ScriptGenerateRequest request
    ) {
        return scriptService.generateScript(request);
    }
}
