package foodiepass.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all controller integration tests.
 *
 * Features:
 * - Loads full Spring application context with @SpringBootTest
 * - Activates 'test' profile which:
 *   - Uses H2 in-memory database
 *   - Disables schedulers (scheduler.enabled=false)
 * - Imports MockExternalDependenciesConfig to mock all external API calls
 * - Provides MockMvc for HTTP request testing
 * - Provides ObjectMapper for JSON serialization
 * - Rolls back database changes after each test with @Transactional
 *
 * Usage:
 * public class MyControllerTest extends BaseControllerTest {
 *     // Test methods can use mockMvc and objectMapper directly
 * }
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockExternalDependenciesConfig.class)
@Transactional
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
