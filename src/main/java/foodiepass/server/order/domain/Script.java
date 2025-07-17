package foodiepass.server.order.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Script {

    private final String travelerScript;
    private final String localScript;
}
