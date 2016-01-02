package org.kuali.rice.web.health;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.health.HealthCheck;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link DatabaseConnectionPoolHealthCheck}
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public class DatabaseConnectionPoolHealthCheckTest {

    private DatabaseConnectionPoolHealthCheck healthCheck;
    private TestGauge gauge;

    @Before
    public void setUp() {
        this.gauge = new TestGauge();
        this.healthCheck = new DatabaseConnectionPoolHealthCheck(gauge, 0.5);
    }

    @Test
    public void testCheck_Healthy_UnderThreshold() throws Exception {
        gauge.setValue(0.25);
        HealthCheck.Result result = healthCheck.check();
        assertTrue("Result should be healthy since value is below threshold", result.isHealthy());
    }

    @Test
    public void testCheck_Unhealhty_OverThreshold() throws Exception {
        gauge.setValue(0.75);
        HealthCheck.Result result = healthCheck.check();
        assertFalse("Result should be unhealthy since value is above threshold", result.isHealthy());
    }

    @Test
    public void testCheck_Unhealhty_EqualThreshold() throws Exception {
        gauge.setValue(0.5);
        HealthCheck.Result result = healthCheck.check();
        assertFalse("Result should be unhealthy since value is equal to the threshold", result.isHealthy());
    }

    private static final class TestGauge implements Gauge<Double> {

        private Double value;

        @Override
        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }

}
