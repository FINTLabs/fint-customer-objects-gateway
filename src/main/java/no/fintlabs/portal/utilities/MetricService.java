package no.fintlabs.portal.utilities;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    private final MeterRegistry meterRegistry;
    private final Counter clientCreateCounter;
    private final Counter clientDeleteCounter;

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        clientCreateCounter = Counter.builder("fint.cog.client.create").register(meterRegistry);
        clientDeleteCounter = Counter.builder("fint.cog.client.delete").register(meterRegistry);
    }

    public void registerClientCreate() {
        clientCreateCounter.increment();
    }

    public void registerClientDelete() {
        clientDeleteCounter.increment();
    }
}
