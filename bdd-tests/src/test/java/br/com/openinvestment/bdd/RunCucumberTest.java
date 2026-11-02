package br.com.openinvestment.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Ponto de entrada dos testes BDD. Executa todas as features em src/test/resources/features.
 * Filtrar por tag: mvn test -Dcucumber.filter.tags="@positivo"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.openinvestment.bdd")
public class RunCucumberTest {
}
