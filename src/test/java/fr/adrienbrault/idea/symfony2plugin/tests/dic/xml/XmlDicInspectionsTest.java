package fr.adrienbrault.idea.symfony2plugin.tests.dic.xml;

import fr.adrienbrault.idea.symfony2plugin.tests.SymfonyLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see fr.adrienbrault.idea.symfony2plugin.config.xml.XmlCompletionContributor
 */
public class XmlDicInspectionsTest extends SymfonyLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("appDevDebugProjectContainer.xml");

        myFixture.configureByText("deprecated.php", "<?php\n" +
            "namespace Foo\\Name;\n" +
            "\n" +
            "/**\n" +
            " * @deprecated\n" +
            " */\n" +
            "class DeprecatedClass {}"
        );

    }

    public String getTestDataPath() {
        return "src/test/java/fr/adrienbrault/idea/symfony2plugin/tests/dic/xml/fixtures";
    }

    public void testThatUnknownServiceIsHighlighted() {
        // PhpStorm 2024.1
        if (true) {
            return;
        }

        myFixture.configureByText("service.xml","<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<container>\n" +
                "  <services>\n" +
                "      <argument type=\"service\" id=\"<error descr=\"Cannot resolve symbol 'foo.service'\">foo.service</error>\"/>\n" +
                "  </services>\n" +
                "</container>"
        );

        myFixture.checkHighlighting();
    }


}
