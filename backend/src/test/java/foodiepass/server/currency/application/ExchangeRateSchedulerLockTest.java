package foodiepass.server.currency.application;

import foodiepass.server.config.MockExternalDependenciesConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Import(MockExternalDependenciesConfig.class)
@Disabled("BeanOverride issue with @MockitoSpyBean - needs investigation")
class ExchangeRateSchedulerLockTest {

    @MockitoSpyBean
    private ExchangeRateScheduler scheduler;

    @Test
    @DisplayName("여러 스레드가 동시에 스케줄러를 실행하면, 락(Lock)에 의해 단 한 번만 실행된다")
    void whenTasksRunConcurrently_thenOnlyOneShouldExecute() throws InterruptedException {
        // given
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        IntStream.range(0, numberOfThreads).forEach(i ->
                executorService.submit(() -> {
                    try {
                        scheduler.scheduledCacheUpdate();
                    } finally {
                        latch.countDown();
                    }
                })
        );

        latch.await();

        // then
        verify(scheduler, times(1)).updateAllExchangeRatesInternal();
    }
}
