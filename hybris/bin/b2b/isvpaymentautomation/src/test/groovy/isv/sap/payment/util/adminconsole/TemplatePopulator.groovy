package isv.sap.payment.util.adminconsole

import groovy.text.GStringTemplateEngine

class TemplatePopulator {

    static populateTemplate(String templateFile, Map map) {
        def resource = TemplatePopulator.class.getResource(templateFile)
        if (resource == null) {
            throw new IllegalArgumentException("Template file not found: $templateFile")
        }

        String templateContent = resource.text
        GStringTemplateEngine engine = new GStringTemplateEngine()
        return engine.createTemplate(templateContent).make(map).toString()
    }
}
