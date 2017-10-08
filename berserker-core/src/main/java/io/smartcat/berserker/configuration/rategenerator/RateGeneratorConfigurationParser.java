package io.smartcat.berserker.configuration.rategenerator;

import io.smartcat.berserker.api.RateGenerator;
import io.smartcat.berserker.rategenerator.ConstantRateGenerator;
import io.smartcat.berserker.rategenerator.RateGeneratorProxy;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Constructs {@link RateGenerator} out of parsed configuration.
 */
public class RateGeneratorConfigurationParser {

    private static final String OFFSET = "offset";
    private static final String RATES = "rates";
    private static final String OUTPUT = "output";

    private final Map<String, Object> rates;
    private final Object outputExpression;
    private Map<String, RateGeneratorProxy> proxyValues;
    private RateGeneratorExpressionParser parser;
    private ReportingParseRunner<RateGenerator> parseRunner;

    /**
     * Constructs rate generator configuration parser with specified configuration.
     *
     * @param config Configuration to parse.
     */
    @SuppressWarnings("unchecked")
    public RateGeneratorConfigurationParser(Map<String, Object> config) {
        checkSectionExistence(config, RATES);
        checkSectionExistence(config, OUTPUT);
        this.rates = (Map<String, Object>) config.get(RATES);
        this.outputExpression = config.get(OUTPUT);
    }

    /**
     * Creates an instance of {@link RateGenerator} based on provided configuration.
     *
     * @return An instance of {@link RateGenerator}.
     */
    @SuppressWarnings({ "unchecked" })
    public RateGenerator build() {
        buildModel();
        return parseRateGeneratorExpression(outputExpression);
    }

    private void buildModel() {
        this.proxyValues = new HashMap<>();
        this.parser = Parboiled.createParser(RateGeneratorExpressionParser.class, proxyValues);
        this.parseRunner = new ReportingParseRunner<>(parser.rateGenerator());
        if (rates != null) {
            createProxies();
            parseRateGenerators();
        }
    }

    private void checkSectionExistence(Map<String, Object> config, String name) {
        if (!config.containsKey(name)) {
            throw new RuntimeException("Configuration must contain '" + name + "' section.");
        }
    }

    private void createProxies() {
        for (Map.Entry<String, Object> entry : rates.entrySet()) {
            proxyValues.put(entry.getKey(), new RateGeneratorProxy());
        }
    }

    private void parseRateGenerators() {
        for (Map.Entry<String, Object> entry : rates.entrySet()) {
            RateGenerator rateGenerator = parseRateGeneratorExpression(entry.getValue());
            RateGeneratorProxy proxy = proxyValues.get(entry.getKey());
            proxy.setDelegate(rateGenerator);
            entry.setValue(proxy);
        }
    }

    private RateGenerator parseRateGeneratorExpression(Object def) {
        // handle String as expression and all other types as primitives
        if (def instanceof String) {
            ParsingResult<RateGenerator> result = parseRunner.run((String) def);
            return result.valueStack.pop();
        } else if (def instanceof Long) {
            return new ConstantRateGenerator((long) def);
        } else if (def instanceof Integer) {
            return new ConstantRateGenerator(((Number) def).longValue());
        } else {
            throw new RuntimeException("Object type not supported: " + def.getClass().getName());
        }
    }
}
