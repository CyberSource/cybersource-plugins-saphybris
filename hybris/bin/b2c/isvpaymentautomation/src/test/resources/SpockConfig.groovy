import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke

runner {
    String suite = System.properties.getProperty('suite')

    // handling suite parameter
    println "got suite parameter: $suite"

    if (suite?.equalsIgnoreCase('smoke'))
    {
        include Smoke
    }
    else if (suite?.equalsIgnoreCase('regression'))
    {
        include Regression
    }
}

