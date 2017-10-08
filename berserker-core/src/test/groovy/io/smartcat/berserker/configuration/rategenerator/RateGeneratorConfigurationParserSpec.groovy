package io.smartcat.berserker.configuration.rategenerator

import io.smartcat.berserker.rategenerator.SineRateGenerator
import io.smartcat.berserker.rategenerator.SquareRateGenerator
import io.smartcat.berserker.rategenerator.TriangleRateGenerator
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification


class RateGeneratorConfigurationParserSpec extends Specification {

    private static final long SECONDS_IN_DAY = 86400L
    private static final long SECONDS_IN_HOUR = 3600L
    private static final long SECONDS_IN_MINUTE = 60L
    private static final long NANOS_IN_SECOND = 1_000_000_000L

    def "should parse number into constant rate generator"() {
        given:
        def config = '''
rates:
  firstRate: 4
output: $firstRate
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(1) == 4
    }

    def "should parse simple addition rate generator"() {
        given:
        def config = '''
rates:
  r: 2.5 + 2
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(1) == 4.5
    }

    def "should parse addition and multiplication with parenthesis rate generator"() {
        given:
        def config = '''
rates:
  r: 2 * (3.5 + 4 )
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(1) == 15
    }

    def "should parse addition and multiplication without parenthesis rate generator"() {
        given:
        def config = '''
rates:
  r: 2 * 3 + 4
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(1) == 10
    }

    def "should parse multiple complex operation rate generators"() {
        given:
        def config = '''
rates:
  a: 1 * 3 + 3 * 2
  b: (2 + 1)*5
  c: $b -$a + 2 * 2
  d: 100 / (5 + 5)
  e: 1
output: $c - $d + $e
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(1) == 1
    }

    def "should parse triangle rate generator"() {
        given:
        def config = '''
rates:
  r: triangle(1d2h3m4s, 0.5, 1.1, 100)
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.delegate.class == TriangleRateGenerator
        rateGenerator.delegate.periodInNanos == (1 * SECONDS_IN_DAY + 2 * SECONDS_IN_HOUR + 3 * SECONDS_IN_MINUTE + 4) * NANOS_IN_SECOND
        rateGenerator.delegate.leftSide == 0.5
        rateGenerator.delegate.minValue == 1.1
        rateGenerator.delegate.maxValue == 100.0
    }

    def "should parse sine rate generator"() {
        given:
        def config = '''
rates:
  r: sin(2h10s, 10.4, 100)
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.delegate.class == SineRateGenerator
        rateGenerator.delegate.periodInNanos == (2 * SECONDS_IN_HOUR + 10) * NANOS_IN_SECOND
        rateGenerator.delegate.multiplier == 10.4
        rateGenerator.delegate.independentConstant == 100
    }

    def "should parse square rate generator"() {
        given:
        def config = '''
rates:
  r: square(10m, 0.7, 10, 1000)
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.delegate.class == SquareRateGenerator
        rateGenerator.delegate.periodInNanos == (10 * SECONDS_IN_MINUTE) * NANOS_IN_SECOND
        rateGenerator.delegate.leftSide == 0.7
        rateGenerator.delegate.lowerValue == 10
        rateGenerator.delegate.upperValue == 1000
    }

    def "should parse combination of several rate generators"() {
        given:
        def config = '''
rates:
  r: triangle(8s, 0.5, 1, 100) * 2 + square(10s, 0.2, 10, 200) + 120 * 4/2
output: $r
'''

        when:
        def rateGenerator = buildGenerator(config)

        then:
        rateGenerator.getRate(0 * NANOS_IN_SECOND) == 252
        rateGenerator.getRate(4 * NANOS_IN_SECOND) == 640
        rateGenerator.getRate(8 * NANOS_IN_SECOND) == 442
    }

    def buildGenerator(config) {
        Yaml yaml = new Yaml()
        new RateGeneratorConfigurationParser(yaml.load(config)).build()
    }
}