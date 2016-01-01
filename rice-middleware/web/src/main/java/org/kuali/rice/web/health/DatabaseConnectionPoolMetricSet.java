package org.kuali.rice.web.health;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.RatioGauge;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.enhydra.jdbc.pool.StandardXAPoolDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnectionPoolMetricSet implements MetricSet {

    private static final Logger LOG = Logger.getLogger(DatabaseConnectionPoolMetricSet.class);

    public static final String ACTIVE = "pool.active";
    public static final String MIN = "pool.min";
    public static final String MAX = "pool.max";
    public static final String USAGE = "pool.usage";

    private final String namePrefix;
    private final DataSource dataSource;

    public DatabaseConnectionPoolMetricSet(String namePrefix, DataSource dataSource) {
        this.namePrefix = namePrefix;
        this.dataSource = dataSource;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<>();
        boolean success = tryXAPool(metrics) || tryBitronix(metrics) || tryDBCP(metrics);
        if (!success) {
            LOG.warn("Failed to identify the type of connection pool with namePrefix: " + namePrefix + " and dataSource class: " + dataSource.getClass());
        }
        return metrics;
    }

    private boolean tryXAPool(Map<String, Metric> metrics) {
        StandardXAPoolDataSource xaPoolDataSource = tryUnwrap(dataSource, StandardXAPoolDataSource.class);
        if (xaPoolDataSource != null) {
            installXAPoolMetrics(xaPoolDataSource, metrics);
            return true;
        }
        return false;
    }

    private void installXAPoolMetrics(final StandardXAPoolDataSource dataSource, Map<String, Metric> metrics) {
        metrics.put(namePrefix + ACTIVE, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getLockedObjectCount();
            }
        });
        metrics.put(namePrefix + MIN, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMinSize();
            }
        });
        metrics.put(namePrefix + MAX, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMaxSize();
            }
        });
        metrics.put(namePrefix + USAGE, new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(dataSource.getLockedObjectCount(), dataSource.getMaxSize());
            }
        });
    }

    private boolean tryBitronix(Map<String, Metric> metrics) {
        PoolingDataSource poolingDataSource = tryUnwrap(dataSource, PoolingDataSource.class);
        if (poolingDataSource != null) {
            installBitronixMetrics(poolingDataSource, metrics);
            return true;
        }
        return false;
    }

    private void installBitronixMetrics(final PoolingDataSource dataSource, Map<String, Metric> metrics) {
        metrics.put(namePrefix + ACTIVE, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return (int)dataSource.getTotalPoolSize() - (int)dataSource.getInPoolSize();
            }
        });
        metrics.put(namePrefix + MIN, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMinPoolSize();
            }
        });
        metrics.put(namePrefix + MAX, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMaxPoolSize();
            }
        });
        metrics.put(namePrefix + USAGE, new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(dataSource.getTotalPoolSize() - dataSource.getInPoolSize(), dataSource.getMaxPoolSize());
            }
        });
    }

    private boolean tryDBCP(Map<String, Metric> metrics) {
        BasicDataSource basicDataSource = tryUnwrap(dataSource, BasicDataSource.class);
        if (basicDataSource != null) {
            installDBCPMetrics(basicDataSource, metrics);
            return true;
        }
        return false;
    }

    private void installDBCPMetrics(final BasicDataSource dataSource, Map<String, Metric> metrics) {
        metrics.put(namePrefix + ACTIVE, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getNumActive();
            }
        });
        metrics.put(namePrefix + MIN, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMinIdle();
            }
        });
        metrics.put(namePrefix + MAX, new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return dataSource.getMaxActive();
            }
        });
        metrics.put(namePrefix + USAGE, new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(dataSource.getNumActive(), dataSource.getMaxActive());
            }
        });
    }

    private <T> T tryUnwrap(DataSource dataSource, Class<T> targetType) {
        if (targetType.isInstance(dataSource)) {
            return targetType.cast(dataSource);
        }
        try {
            if (dataSource.isWrapperFor(targetType)) {
                return dataSource.unwrap(targetType);
            }
        } catch (SQLException e) {
            LOG.warn("Exception when trying to unwrap datasource as " + targetType, e);
        }
        return null;
    }

}
