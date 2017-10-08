package io.smartcat.berserker.configuration.rategenerator;

import io.smartcat.berserker.api.RateGenerator;
import io.smartcat.berserker.rategenerator.*;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.Map;

/**
 * Parser for configuration rate generator expressions.
 */
public class RateGeneratorExpressionParser extends BaseParser<Object> {

    private static final long SECONDS_IN_DAY = 86400;
    private static final long SECONDS_IN_HOUR = 3600;
    private static final long SECONDS_IN_MINUTE = 60;

    private final Map<String, RateGeneratorProxy> proxyValues;

    /**
     * Constructs parser with initial <code>proxyValues</code>.
     *
     * @param proxyValues Map containing proxy values by name.
     */
    public RateGeneratorExpressionParser(Map<String, RateGeneratorProxy> proxyValues) {
        this.proxyValues = proxyValues;
    }

    /**
     * Whitespace definition.
     *
     * @return Whitespace definition rule.
     */
    public Rule whitespace() {
        return AnyOf(" \t");
    }

    /**
     * Comma definition.
     *
     * @return Comma definition rule.
     */
    public Rule comma() {
        return Sequence(ZeroOrMore(whitespace()), ",", ZeroOrMore(whitespace()));
    }

    /**
     * Open parenthesis definition.
     *
     * @return Open parenthesis definition rule.
     */
    public Rule openParenthesis() {
        return Sequence(ZeroOrMore(whitespace()), "(", ZeroOrMore(whitespace()));
    }

    /**
     * Closed parenthesis definition.
     *
     * @return Closed parenthesis definition rule.
     */
    public Rule closedParenthesis() {
        return Sequence(ZeroOrMore(whitespace()), ")", ZeroOrMore(whitespace()));
    }

    /**
     * Sign definition.
     *
     * @return Sign definition rule.
     */
    public Rule sign() {
        return AnyOf("+-");
    }

    /**
     * Letter definition.
     *
     * @return Letter definition rule.
     */
    public Rule letter() {
        return FirstOf('_', CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    /**
     * Digit definition.
     *
     * @return Digit definition rule.
     */
    public Rule digit() {
        return CharRange('0', '9');
    }

    /**
     * Letter or digit definition.
     *
     * @return Letter or digit definition rule.
     */
    public Rule letterOrDigit() {
        return FirstOf(letter(), digit());
    }

    /**
     * Unsigned integer definition.
     *
     * @return Unsigned integer definition rule.
     */
    public Rule unsignedIntegerLiteral() {
        return OneOrMore(digit());
    }

    /**
     * Exponent definition.
     *
     * @return Exponent definition rule.
     */
    public Rule exponent() {
        return Sequence(AnyOf("eE"), Optional(sign()), unsignedIntegerLiteral());
    }

    /**
     * Long integer definition.
     *
     * @return Long integer definition rule.
     */
    public Rule longLiteral() {
        return Sequence(Sequence(Optional(sign()), unsignedIntegerLiteral()), push(Long.parseLong(match())));
    }

    /**
     * Double definition.
     *
     * @return Double definition rule.
     */
    public Rule doubleLiteral() {
        return Sequence(
                Sequence(Optional(sign()),
                        FirstOf(Sequence(unsignedIntegerLiteral(), '.', unsignedIntegerLiteral(), Optional(exponent())),
                                Sequence('.', unsignedIntegerLiteral(), Optional(exponent())))),
                push(Double.parseDouble(match())));
    }

    /**
     * Number definition.
     *
     * @return Number definition rule.
     */
    public Rule numberLiteral() {
        return FirstOf(doubleLiteral(), longLiteral());
    }

    /**
     * Day duration literal definition.
     *
     * @return Day duration literal definition rule.
     */
    public Rule dayDurationLiteral() {
        return Sequence(unsignedIntegerLiteral(), "d",
                Optional(FirstOf(hourDurationLiteral(), minuteDurationLiteral(), secondDurationLiteral())));
    }

    /**
     * Hour duration literal definition.
     *
     * @return Hour duration literal definition rule.
     */
    public Rule hourDurationLiteral() {
        return Sequence(unsignedIntegerLiteral(), "h",
                Optional(FirstOf(minuteDurationLiteral(), secondDurationLiteral())));
    }

    /**
     * Minute duration literal definition.
     *
     * @return Minute duration literal definition rule.
     */
    public Rule minuteDurationLiteral() {
        return Sequence(unsignedIntegerLiteral(), "m", Optional(secondDurationLiteral()));
    }

    /**
     * Second duration literal definition.
     *
     * @return Second duration literal definition rule.
     */
    public Rule secondDurationLiteral() {
        return Sequence(unsignedIntegerLiteral(), "s");
    }

    /**
     * Duration literal definition.
     *
     * @return Duration literal definition rule.
     */
    public Rule durationLiteral() {
        return Sequence(
                FirstOf(dayDurationLiteral(), hourDurationLiteral(), minuteDurationLiteral(), secondDurationLiteral()),
                push(createDuration(match())));
    }

    /**
     * Constant rate generator definition.
     *
     * @return Constant rate generator definition rule.
     */
    public Rule constantRateGenerator() {
        return Sequence(numberLiteral(), push(new ConstantRateGenerator(((Number) pop()).doubleValue())));
    }

    /**
     * Identifier definition which does not push match to value stack.
     *
     * @return Identifier definition rule.
     */
    public Rule identifierWithNoPush() {
        return Sequence(letter(), ZeroOrMore(letterOrDigit()));
    }

    /**
     * Function definition.
     *
     * @param functionName Name of a function.
     * @return Function definition rule.
     */
    public Rule function(String functionName) {
        return function(functionName, fromStringLiteral(""));
    }

    /**
     * Function definition.
     *
     * @param functionArgument Function argument rule.
     * @return Function definition rule.
     */
    public Rule function(Rule functionArgument) {
        return function("", functionArgument);
    }

    /**
     * Function definition.
     *
     * @param functionName Name of a function.
     * @param functionArgument Function argument rule.
     * @return Function definition rule.
     */
    public Rule function(String functionName, Rule functionArgument) {
        return Sequence(functionName, openParenthesis(), functionArgument, closedParenthesis());
    }

    /**
     * Rate generator reference definition.
     *
     * @return Rate generator reference definition rule.
     */
    public Rule rateGeneratorReference() {
        return Sequence('$', identifierWithNoPush(), push(getRateGeneratorProxy(match())));
    }

    /**
     * Triangle rate generator definition.
     *
     * @return Triangle rate generator definition rule.
     */
    public Rule triangleRateGenerator() {
        return Sequence(function("triangle", Sequence(durationLiteral(), comma(), numberLiteral(), comma(),
                numberLiteral(), comma(), numberLiteral())), push(createTriangleRateGenerator()));
    }

    /**
     * Sine rate generator definition.
     *
     * @return Sine rate generator definition rule.
     */
    public Rule sineRateGenerator() {
        return Sequence(
                function("sin", Sequence(durationLiteral(), comma(), numberLiteral(), comma(), numberLiteral())),
                push(createSineRateGenerator()));
    }

    /**
     * Square rate generator definition.
     *
     * @return Square rate generator definition rule.
     */
    public Rule squareRateGenerator() {
        return Sequence(function("square", Sequence(durationLiteral(), comma(), numberLiteral(), comma(),
                numberLiteral(), comma(), numberLiteral())), push(createSquareRateGenerator()));
    }

    /**
     * Complex rate generator definition.
     *
     * @return Complex rate generator definition rule.
     */
    public Rule functionRateGenerator() {
        return FirstOf(triangleRateGenerator(), sineRateGenerator(), squareRateGenerator());
    }

    /**
     * Rate generator definition.
     *
     * @return rate generator definition rule.
     */
    public Rule rateGenerator() {
        Var<Character> op = new Var<>();
        return Sequence(term(), ZeroOrMore(ZeroOrMore(whitespace()), FirstOf("+", "-"), op.set(matchedChar()),
                ZeroOrMore(whitespace()), term(), push(createAdditionOrSubtractionRateGenerator(op.get()))));
    }

    /**
     * Term definition.
     *
     * @return Term definition rule.
     */
    public Rule term() {
        Var<Character> op = new Var<>();
        return Sequence(factor(), ZeroOrMore(ZeroOrMore(whitespace()), FirstOf("*", "/"), op.set(matchedChar()),
                ZeroOrMore(whitespace()), factor(), push(createMultiplicationOrDivisionRateGenerator(op.get()))));
    }

    /**
     * Factor definition.
     *
     * @return Factor definition rule.
     */
    public Rule factor() {
        return FirstOf(simpleRateGenerator(), function(rateGenerator()));
    }

    /**
     * Simple rate generator definition.
     *
     * @return Simple rate generator definition rule.
     */
    public Rule simpleRateGenerator() {
        return FirstOf(rateGeneratorReference(), functionRateGenerator(), constantRateGenerator());
    }

    /**
     * Returns or creates new value proxy for given name.
     *
     * @param name Name of the value proxy.
     * @return Proxy value.
     */
    protected RateGenerator getRateGeneratorProxy(String name) {
        if (proxyValues.containsKey(name)) {
            return proxyValues.get(name);
        }
        throw new InvalidReferenceNameException(name);
    }

    /**
     * Creates duration in seconds out of duration expression (e.g. <code>d2h3m4s</code>).
     *
     * @param durationExpression Duration expression from which to create duration.
     * @return Long number representing duration in seconds.
     */
    protected long createDuration(String durationExpression) {
        String durationExpr = durationExpression;
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if (durationExpr.contains("d")) {
            days = Long.parseLong(durationExpr.substring(0, durationExpr.indexOf("d")));
            durationExpr = durationExpr.substring(durationExpr.indexOf("d") + 1);
        }
        if (durationExpr.contains("h")) {
            hours = Long.parseLong(durationExpr.substring(0, durationExpr.indexOf("h")));
            durationExpr = durationExpr.substring(durationExpr.indexOf("h") + 1);
        }
        if (durationExpr.contains("m")) {
            minutes = Long.parseLong(durationExpr.substring(0, durationExpr.indexOf("m")));
            durationExpr = durationExpr.substring(durationExpr.indexOf("m") + 1);
        }
        if (durationExpr.contains("s")) {
            seconds = Long.parseLong(durationExpr.substring(0, durationExpr.indexOf("s")));
        }
        return days * SECONDS_IN_DAY + hours * SECONDS_IN_HOUR + minutes * SECONDS_IN_MINUTE + seconds;
    }

    /**
     * Creates either {@link AdditionRateGenerator} or {@link SubtractionRateGenerator} based on operation parameter.
     * Throws exception if operation is anything else.
     *
     * @param operation Can be either <code>'+'</code> or <code>'-'</code>.
     * @return An instance of {@link AdditionRateGenerator} or {@link SubtractionRateGenerator}.
     */
    protected RateGenerator createAdditionOrSubtractionRateGenerator(char operation) {
        RateGenerator operator1 = (RateGenerator) pop(1);
        RateGenerator operator2 = (RateGenerator) pop();
        if (operation == '+') {
            return new AdditionRateGenerator(operator1, operator2);
        }
        if (operation == '-') {
            return new SubtractionRateGenerator(operator1, operator2);
        }
        throw new RuntimeException("Illegal character used for operation: " + operation);
    }

    /**
     * Creates either {@link MultiplicationRateGenerator} or {@link DivisionRateGenerator} based on operation parameter.
     * Throws exception if operation is anything else.
     *
     * @param operation Can be either <code>'*'</code> or <code>'/'</code>.
     * @return An instance of {@link MultiplicationRateGenerator} or {@link DivisionRateGenerator}.
     */
    protected RateGenerator createMultiplicationOrDivisionRateGenerator(char operation) {
        RateGenerator operator1 = (RateGenerator) pop(1);
        RateGenerator operator2 = (RateGenerator) pop();
        if (operation == '*') {
            return new MultiplicationRateGenerator(operator1, operator2);
        }
        if (operation == '/') {
            return new DivisionRateGenerator(operator1, operator2);
        }
        throw new RuntimeException("Illegal character used for operation: " + operation);
    }

    /**
     * Creates triangle rate generator.
     *
     * @return An instance of {@link TriangleRateGenerator}.
     */
    protected RateGenerator createTriangleRateGenerator() {
        long periodInSeconds = (long) pop(3);
        double leftSide = ((Number) pop(2)).doubleValue();
        double minValue = ((Number) pop(1)).doubleValue();
        double maxValue = ((Number) pop()).doubleValue();
        return new TriangleRateGenerator(periodInSeconds, leftSide, minValue, maxValue);
    }

    /**
     * Creates sine rate generator.
     *
     * @return An instance of {@link SineRateGenerator}.
     */
    protected RateGenerator createSineRateGenerator() {
        long periodInSeconds = (long) pop(2);
        double multiplier = ((Number) pop(1)).doubleValue();
        double independentConstant = ((Number) pop()).doubleValue();
        return new SineRateGenerator(periodInSeconds, multiplier, independentConstant);
    }

    /**
     * Creates square rate generator.
     *
     * @return An instance of {@link SquareRateGenerator}.
     */
    protected RateGenerator createSquareRateGenerator() {
        long periodInSeconds = (long) pop(3);
        double leftSide = ((Number) pop(2)).doubleValue();
        double lowerValue = ((Number) pop(1)).doubleValue();
        double upperValue = ((Number) pop()).doubleValue();
        return new SquareRateGenerator(periodInSeconds, leftSide, lowerValue, upperValue);
    }
}
