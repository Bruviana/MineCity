package br.com.gamemods.minecity.api.command;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import static br.com.gamemods.minecity.api.command.LegacyFormat.*;
import static org.junit.Assert.assertEquals;

public class MessageTransformerTest
{
    MessageTransformer transformer;
    @Before
    public void setUp() throws Exception
    {
        transformer = new MessageTransformer();
        transformer.parseXML(getClass().getResourceAsStream("/assets/minecity/test-messages.xml"));
    }

    @Test
    public void testLegacy() throws Exception
    {
        Element element = transformer.getElement("test.bold").get();
        assertEquals("bold", element.getAttribute("id"));

        Message message = new Message("test.bold");
        assertEquals("This message has a bold word", transformer.toSimpleText(message));

        assertEquals("A "+BOLD+"bold"+RESET+" word", transformer.toLegacy("<msg>A <b>bold</b> word</msg>"));
        assertEquals("Has "+BOLD+"bold and "+RESET+BOLD+ITALIC+"italic"+RESET+" words",
                transformer.toLegacy("<msg>Has <b>bold and <i>italic</i></b> words</msg>"));

        assertEquals("Has "+BOLD+"bold and "+RESET+BOLD+ITALIC+"italic"+RESET+BOLD+" words",
                transformer.toLegacy("<msg>Has <b>bold and <i>italic</i> words</b></msg>"));

        assertEquals(BOLD+"Bold"+RESET+" spaces", transformer.toLegacy("<msg><b>Bold</b>     spaces</msg>"));
        assertEquals(BOLD+"Bold"+RESET+"     spaces", transformer.toLegacy("<msg><b>Bold</b><![CDATA[     spaces]]></msg>"));
        assertEquals("A "+BOLD+"Bold"+RESET+" word", transformer.toLegacy("<msg>\n\tA\n\t<b>Bold</b>\n\tword\n</msg>"));
    }

    @Test
    public void testInline() throws Exception
    {
        Message inline = new Message("", "inline");
        Message container = new Message("", "This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals("This contains an inline message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", RED+"inline");
        container = new Message("", "This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals("This contains an "+RED+"inline"+RESET+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", RED+"inline");
        container = new Message("", GREEN+"This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+RED+"inline"+GREEN+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", GREEN+"This contains an "+BOLD+"${msg} message", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+BOLD+RED+BOLD+"inline"+GREEN+BOLD+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg>inline</msg>");
        container = new Message("", "<msg>This contains an ${msg} message</msg>", new Object[]{"msg", inline});
        assertEquals("This contains an inline message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg><red>inline</red></msg>");
        container = new Message("", "<msg>This contains an ${msg} message</msg>", new Object[]{"msg", inline});
        assertEquals("This contains an "+RED+"inline"+RESET+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg><red>inline</red></msg>");
        container = new Message("", "<msg><green>This contains an ${msg} message</green></msg>", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+GREEN+RED+"inline"+GREEN+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", "<msg><green>This contains an <b>${msg} message</b></green></msg>", new Object[]{"msg", inline});
        String result = transformer.toLegacy(container);
        assertEquals(GREEN+"This contains an "+RESET+GREEN+BOLD+GREEN+BOLD+RED+BOLD+BOLD+"inline"+GREEN+BOLD+" message",result);
        assertEquals("This contains an inline message", transformer.toSimpleText(container));
    }

    @Test
    public void testSkip() throws Exception
    {
        Message msg = new Message("", "<msg><hover><tooltip><b>Title</b><br/><i>Text</i></tooltip><b>Mouse hover here</b></hover></msg>");
        assertEquals(BOLD+"Mouse hover here", transformer.toLegacy(msg));
    }
}
