# Rate Generator Configuration

Berserker supports configuration of rate generator through YAML file. Basic file structure is following:

```yaml
rates:
  rateA: 10
output: $rateA
```
There can be arbitrary number of rate generator definitions within `rates` property. Final output rate generator that will be used is references under `output` property.
Rate generator is represented in number of operations per second, or to be more precise, number of times DataSource's `getNext()` method and Worker's `accept()` method will be invoked per second.

# Primitive rate generators

Rate generator can be represented as primitive number:

```yaml
rates:
  rateA: 10
  rateB: 20.5
```

This will construct two rate generators `rateA` and `rateB` with rates of `10` and `20.5` respectively.

# Reference rate generators

Once defined, rate generator can be reused by its reference:

```yaml
rates:
  rateA: 10
  rateB: $rateA
```

This example might not illustrate full potential of references or might not present right use case, but it is actually really useful when used with [combining rate generators](#combining-rate-generators).

# Function rate generators

Besides [primitive rate generators](#primitive-rate-generators), functions can be defined. Functions are defined as functions of time where result represents rate.

## Period literal

Before functions are explained, lets take a look at period literal.
Period literal is used as argument to define duration of period in all periodic functions, here are few examples with explanations:
                                                                                           
```
2d10m = 2 days and 10 minutes
2d5h1s = 2 days, 5 hours and 1 second
1d2h3m4s = 1 day, 2 hours, 3 minutes and 4 seconds
2h = 2 hours
10s = 10 seconds
```
   
Period literal does not need to contain all time units (days, hours, minutes and seconds), but it must contain at least one. However, time units must be in day-hour-minute-second order with possibility of omitting any of it.

## Triangle function

Triangle function behaves as same-named period wave ([Triangle wave](https://en.wikipedia.org/wiki/Triangle_wave)) with generalization that it can represent also [Sawtooth wave](https://en.wikipedia.org/wiki/Sawtooth_wave).
It is defined with four parameters as follows:

```yaml
rates:
  rateA: triangle(2d10m, 0.2, 100, 15000)
```

First parameter represents period duration. Period duration is defined with [period literal](#period-literal).
Second parameter represents percentage of the period where function is in ascending slope, or where maximum lies. Function starts from minimum and raising to maximum, and then again lowering to minimum, that is where period ends.
Third parameter represents minimum value.
Fourth parameter represents maximum value.

Examples:

```yaml
rates:
  rateA: triangle(10s, 0.3, 10, 20)
  rateB: triangle(20, 0.5, 1, 100)
```

Rate generator `rateA` starts from value `10` (minimum) it raises from `10` to `20` and at second 3 is at value `20` (maximum). Afterwards, rate is dropping from `20` to `10` for next 7 seconds. And then cycle starts again.
Rate generator `rateB` starts from value `1` (minimum) it raises from `1` to `100` and at second 10 is at value `100` (maximum). Afterwards, rate is dropping from `100` to `1` for next 10 seconds. And then cycle starts again.

## Sine function

Sine function behaves as sine wave ([Sine wave](https://en.wikipedia.org/wiki/Sine_wave)).
It is defined with 3 parameters as follows:

```yaml
rates:
  rateA: sin(10h, 200, 150)
```

First parameter represents period duration. Period duration is defined with [period literal](#period-literal).
Second parameter represents multiplier by which sine function will be multiplied (instead of having values between [1, -1], it will have values between [multiplier, -multiplier]). But be aware that rate cannot be negative, so any number lower than 0 will be treated as 0. That's where third parameter comes into play.
Third parameter represents independent constant which will be added to sine function value. This will allow for sine shape wave that can have positive number for minimum.

Examples:

```yaml
rates:
  rateA: sin(10s, 100, 101)
  rateB: sin(20s, 1000, 2000)
```

Rate generator `rateA` generates sine signal where minimum is at 1 and maximum is at 201
Rate generator `rateB` generates sine signal where minimum is at 1000 and maximum is at 3000.

## Square function

Square function behaves as square wave ([Square wave](https://en.wikipedia.org/wiki/Square_wave)).
It is defined with 4 parameters as follows:

```yaml
rates:
  rateA: square(20s, 0.4, 10, 100)
```

First parameter represents period duration. Period duration is defined with [period literal](#period-literal).
Second parameter represents percentage of the period where function returns lower value. Function returns lower value until percentage is reached. When percentage is reached, function returns upper value until the end of period.
Third parameter represents lower value.
Fourth parameter represents upper value.

Examples:

```yaml
rates:
  rateA: square(20s, 0.2, 10, 20)
  rateB: square(10, 0.6, 5, 150)
```

Rate generator `rateA` returns value 10 for first 4 seconds, afterwards, it returns value 20 until the end of period (rest 16 seconds).
Rate generator `rateB` returns value 5 for first 6 seconds, afterwards, it returns value 150 until the end of period (rest 4 seconds).

# Combining rate generators

While rate generators can be defined as primitive values or as functions, real power comes when they are combined. That can be achieved with simple mathematical operations. This can be best explained with examples:

```yaml
rates:
  rateA: 4 + 10
  rateB: square(10s, 0.4, 100, 1000) + 2 * square(10s, 0.5, 10, 50)
  rateC: 2 * ($rateA + $rateB)
  rateD: ($rateC - 10.5) / 2
```

As shown in examples, primitive, function and reference rate generators can be combined with any of the following operations (+, -, *, /) taking into account operation precedence. Also precedence can be enforced using parentheses. 