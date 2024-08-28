package com.ericsson.oss.rfa.data.metric;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.testng.collections.Maps;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.DataSourceCsvRenderer;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.AbstractScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;

public class Metric extends AbstractScenarioListener implements ScenarioListener {

    private static final String METRICS = "metrics";

    public static void registerDataSource() {
        if (!TafTestContext.getContext().doesDataSourceExist(METRICS)) {
            TestDataSource<DataRecord> ds = TafTestContext.getContext().dataSource(METRICS);
            TafDataSources.shared(ds);
            TafTestContext.getContext().addDataSource(METRICS, ds);
        }
    }

    public static class MetricDataBuilder {
        private final Map<String, Object> values = Maps.newHashMap();

        public static MetricDataBuilder builder() {
            return new MetricDataBuilder();
        }

        public MetricDataBuilder data(String name, Object value) {
            values.put(name, value);
            return this;
        }

        public void report() {
            DataRecordModifier sample = TafTestContext.getContext().dataSource(METRICS)
                    .addRecord()
                    .setField("time", new Date())
                    .setField("vuser", TafTestContext.getContext().getVUser());
            for (Map.Entry<String, Object> value : values.entrySet()) {
                sample.setField(value.getKey(), Objects.toString(value.getValue()));
            }

        }
    }

    private final DataSourceCsvRenderer renderer = new DataSourceCsvRenderer();

    @Override
    public void onScenarioStarted(TestScenario scenario) {
        registerDataSource();
    }

    @Override
    public void onScenarioFinished(TestScenario scenario) {
        write();
    }

    private void write() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(String.format("%s.csv", METRICS)));
            renderer.render(writer, TafTestContext.getContext().dataSource(METRICS));
        } catch (IOException e) {
        } finally {

            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }

    }

}
