package isv.sap.payment.util.adminconsole

import groovy.text.GStringTemplateEngine

class TemplatePopulator
{

    static populateTemplate(String templateFile, Map map)
    {
        String templateContent = getClass().getResource(templateFile).text

        GStringTemplateEngine engine = new GStringTemplateEngine()
        engine.createTemplate(templateContent).make(map).toString()
    }
}

