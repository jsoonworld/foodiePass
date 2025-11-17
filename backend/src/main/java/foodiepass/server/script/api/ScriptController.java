package foodiepass.server.script.api;

import foodiepass.server.script.application.ScriptService;
import foodiepass.server.script.dto.request.ScriptGenerateRequest;
import foodiepass.server.script.dto.response.ScriptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scripts")
public class ScriptController {

    private final ScriptService scriptService;

    @PostMapping("/generate")
    public Mono<ScriptResponse> generateScript(
            @RequestBody final ScriptGenerateRequest request
    ) {
        return scriptService.generateScript(request);
    }
}
