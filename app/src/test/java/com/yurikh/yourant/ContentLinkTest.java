package com.yurikh.yourant;

import com.yurikh.yourant.model.ContentLink;

import static org.junit.Assert.*;
import org.junit.Test;

public class ContentLinkTest {
    @Test
    public void toXmlIsCorrect() {
        ContentLink link1 = newLink("http://example.com/path", "example.com...");
        ContentLink link2 = newLink("FULL", "SHORT");

        assertEquals("<string><a href=\"http://example.com/path\">example.com...</a></string>", link1.toXmlLink());
        assertEquals("<string><a href=\"FULL\">SHORT</a></string>", link2.toXmlLink());
        assertNotEquals("randomBullshit", link2.toXmlLink());
    }

    @Test
    public void embeddingIsCorrect() {
        String text =
                "Here is some image: https://img.devrant.com/...\n" +
                "And here is some wiki: https://wikipedia.com/...\n" +
                "Some molodetz link: https://molodetz.nl/...\n" +
                "And some text";

        String expected =
                "Here is some image: <string><a href=\"https://img.devrant.com/some_image.png\">https://img.devrant.com/...</a></string>\n" +
                "And here is some wiki: <string><a href=\"https://wikipedia.com/some_article\">https://wikipedia.com/...</a></string>\n" +
                "Some molodetz link: <string><a href=\"https://molodetz.nl/some_page\">https://molodetz.nl/...</a></string>\n" +
                "And some text";

        int i1 = text.indexOf("https://img.devrant.com/...");
        int i2 = text.indexOf("https://wikipedia.com/...");
        int i3 = text.indexOf("https://molodetz.nl/...");
        int l1 = i1 + "https://img.devrant.com/...".length();
        int l2 = i2 + "https://wikipedia.com/...".length();
        int l3 = i3 + "https://molodetz.nl/...".length();

        ContentLink[] links = {
            newLink("https://img.devrant.com/some_image.png", "https://img.devrant.com/...", i1, l1),
            newLink("https://wikipedia.com/some_article", "https://wikipedia.com/...", i2, l2),
            newLink("https://molodetz.nl/some_page", "https://molodetz.nl/...", i3, l3)
        };

        // Replace backwards because replacing changes size of text and so do the indices.
        // If you replace forwards, the index of the next link has been changed already by the previous.
        for (int i = links.length - 1; i >= 0; i--) {
            text = links[i].embedIntoString(text);
        }

        assertEquals(expected, text);
        assertNotEquals("expected", text);
    }

    public ContentLink newLink(String url, String short_url) {
        return new ContentLink("url", url, short_url, "title", 0, 0);
    }
    public ContentLink newLink(String url, String short_url, int start, int end) {
        return new ContentLink("url", url, short_url, "title", start, end);
    }
}
