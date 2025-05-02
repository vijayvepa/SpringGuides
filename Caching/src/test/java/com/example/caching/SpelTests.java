package com.example.caching;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.util.GregorianCalendar;
import java.util.Objects;

public class SpelTests {

  @Test
  void concatTest(){
    String test = parseSpel("'Hello World'.concat('!')");
    Assertions.assertThat(test).isNull();
  }

  @Test
  void bytesTest(){

    byte[] bytes = parseSpel("'Hello World'.bytes");

    Assertions.assertThat(bytes).isEmpty();
  }
  @Test
  void bytesLengthTest(){

    int bytes = parseSpel("'Hello World'.bytes.length");

    Assertions.assertThat(bytes).isEqualTo(0);
  }

  @Test
  void propertiesTest() {
    GregorianCalendar c = new GregorianCalendar();
    c.set(1856, 7, 9);
    Inventor tesla = new Inventor("Nikola Tesla", c.getTime(), "Serbian");
    ExpressionParser parser = new SpelExpressionParser();
    final Expression expression = parser.parseExpression("name");
    String name = (String) expression.getValue(tesla);

    Assertions.assertThat(name).isEqualTo("Nikola Tesla");

    final Expression equality = parser.parseExpression("name == 'Nikola Tesla' ");
    Boolean equalityValue = equality.getValue(tesla, Boolean.class);

    Assertions.assertThat(equalityValue).isTrue();
  }


  @Test
  void conversionTest() {
    Simple simple= new Simple();
    simple.booleans.add(true);
    EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

    ExpressionParser parser = new SpelExpressionParser();
    parser.parseExpression("booleans[0]")
        .setValue(context, simple, false);
    Assertions.assertThat(simple.booleans.get(0)).isFalse();

  }

  @Test
  void configutationTest(){
    SpelParserConfiguration configuration = new SpelParserConfiguration(
        true, true);
    ExpressionParser expressionParser = new SpelExpressionParser(configuration);
    Expression expression = expressionParser.parseExpression("list[3]");
    Demo demo = new Demo();
    Object o = expression.getValue(demo);

    Assertions.assertThat(demo.list).isNull();

  }

  private static <T>  T parseSpel(String expressionString) {
    ExpressionParser parser = new SpelExpressionParser();
    Expression expression = parser.parseExpression(expressionString);
    return (T) expression.getValue() ;
  }
}

