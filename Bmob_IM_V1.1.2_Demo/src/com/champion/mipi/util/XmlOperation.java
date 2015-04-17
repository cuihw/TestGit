package com.champion.mipi.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class XmlOperation {

    private static final String TAG = "XmlOperation";

    public static String getAttriValueByTag(String xml, String tag, String attri) {

        XmlPullParser pullParser = Xml.newPullParser();
        
        String retString = null;

        try {
            pullParser.setInput(new StringReader(xml));
            int event = pullParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {

                switch (event) {

                    case XmlPullParser.START_TAG:

                        if (tag.equals(pullParser.getName())) {

                            int count = pullParser.getAttributeCount();
                            for (int i = 0; i < count; i++) {
                                String name = pullParser.getAttributeName(i);
                                if (name.equals(attri)) {
                                    retString = pullParser.getAttributeValue(i);
                                    return retString;
                                }
                            }
                        }
                        break;
                    case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.END_TAG:
                        break;
                }

                event = pullParser.next();
            }

        } catch (XmlPullParserException e) {
            Log.e(TAG, "exception: " + e);
            e.printStackTrace();
            return retString;
        } catch (IOException e) {
            Log.e(TAG, "exception: " + e);
            e.printStackTrace();
            return retString;
        }

        return retString;
    }
    
    public static String getValueByTag(String xml, String tag) {

        XmlPullParser pullParser = Xml.newPullParser();
        
        String retString = null;

        try {
            pullParser.setInput(new StringReader(xml));
            int event = pullParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {

                switch (event) {

                    case XmlPullParser.START_TAG:

                        if (tag.equals(pullParser.getName())) {

                            retString = pullParser.nextText();

                            return retString;
                        }
                        break;
                    case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.END_TAG:
                        break;
                }

                event = pullParser.next();
            }

        } catch (XmlPullParserException e) {
            Log.e(TAG, "exception: " + e);
            e.printStackTrace();
            return retString;
        } catch (IOException e) {
            Log.e(TAG, "exception: " + e);
            e.printStackTrace();
            return retString;
        }

        return retString;
    }
    /*<cmd name="reg">
        <ip>192.168.1.10</ip>
        <name>username</name>
        <nickname>hellokitty</nickname>
    </cmd>
    */
    public static String buildCmd(String cmd, Map<String, String> value) {
        String cmdString = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        Element theCmd=null, theElem = null;
        try {

            DocumentBuilder db = factory.newDocumentBuilder();
            Document xmldoc = db.newDocument();

            theCmd = xmldoc.createElement("cmd");
            theCmd.setAttribute("name", cmd);
            Set<String> keys = value.keySet();

            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                theElem=xmldoc.createElement(key);
                theElem.setTextContent(value.get(key));
                theCmd.appendChild(theElem);
            }

            // write to the buffer.
            StringWriter writer = new StringWriter();

            TransformerFactory.newInstance().newTransformer()
                    .transform(new DOMSource(theCmd), new StreamResult(writer));

            cmdString = writer.toString();
            Log.d(TAG, "xml String is : " + cmdString);

        } catch (ParserConfigurationException e) {

            e.printStackTrace();
        }catch (TransformerConfigurationException e) {

            e.printStackTrace();
        } catch (TransformerException e) {

            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {

            e.printStackTrace();
        }     

        return  cmdString;
    } 
}
