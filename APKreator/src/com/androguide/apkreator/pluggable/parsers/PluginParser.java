package com.androguide.apkreator.pluggable.parsers;

import android.os.AsyncTask;

import com.androguide.apkreator.pluggable.objects.Config;
import com.androguide.apkreator.pluggable.objects.Tweak;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class PluginParser {
    ArrayList<Tweak> tweaks;
    ArrayList<Config> configs;
    private Tweak tweak;
    private Config config;
    private String text;

    public PluginParser() {
        tweaks = new ArrayList<Tweak>();
        configs = new ArrayList<Config>();
    }

    public List<Tweak> getTweaks() {
        return tweaks;
    }

    /**
     * @param is An InputStream of the XML file we want to open
     * @return a Collection of Tweak Objects, each delimited by <tweak></tweak> tags in the XML
     */
    public List<Tweak> parse(InputStream is) {
        try {
            return new AsyncTweaksParsing().execute(is).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<Tweak>();
    }

    /**
     * @param is An InputStream of the XML file we want to open
     * @return a Collection of Config Object, each delimited by <tweak></tweak> tags in the XML
     */
    public List<Config> parseConfig(InputStream is) {
        try {
            return new AsyncConfigParsing().execute(is).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<Config>();
    }

    /**
     * Do the parsing in an AsyncTask in order to avoid blocking the UI thread
     * (parsing is an expensive operation)
     */
    private class AsyncTweaksParsing extends AsyncTask<InputStream, Void, ArrayList<Tweak>> {

        @Override
        protected ArrayList<Tweak> doInBackground(InputStream... streams) {
            XmlPullParserFactory factory;
            XmlPullParser parser;
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newPullParser();
                parser.setInput(streams[0], null);

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {

                        // OPENING TAG
                        case XmlPullParser.START_TAG:
                            if (tagName.equalsIgnoreCase("plugin"))
                                tweak = new Tweak();
                            else if (tagName.equalsIgnoreCase("tweak")) {
                                tweak = new Tweak();
                                tweak.setType(parser.getAttributeValue(null, "type"));
                            } else if (tagName.equalsIgnoreCase("control")) {
                                tweak.setControl(parser.getAttributeValue(null, "type"));
                                tweak.setBooleanOn(parser.getAttributeValue(null, "on"));
                                tweak.setBooleanOff(parser.getAttributeValue(null, "off"));
                            }
                            break;

                        // ENCLOSED TEXT
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;

                        // CLOSING TAG
                        case XmlPullParser.END_TAG:
                            if (tagName.equalsIgnoreCase("tweak"))
                                tweaks.add(tweak);
                            else if (tagName.equalsIgnoreCase("name"))
                                tweak.setName(text);
                            else if (tagName.equalsIgnoreCase("description"))
                                tweak.setDesc(text);
                            else if (tagName.equalsIgnoreCase("unit"))
                                tweak.setUnit(text);
                            else if (tagName.equalsIgnoreCase("prop"))
                                tweak.setProp(text);
                            else if (tagName.equalsIgnoreCase("min-value"))
                                tweak.setMin(Integer.parseInt(text));
                            else if (tagName.equalsIgnoreCase("max-value"))
                                tweak.setMax(Integer.parseInt(text));
                            else if (tagName.equalsIgnoreCase("default-value"))
                                tweak.setDef(Integer.parseInt(text));
                            break;
                        default:
                            break;
                    }
                    eventType = parser.next();
                }

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return tweaks;
        }

        protected void onPostExecute(ArrayList<Tweak> result) {
            tweaks = result;
        }

    }

    /**
     * Do the parsing in an AsyncTask in order to avoid blocking the UI thread (parsing is an expensive operation)
     */
    private class AsyncConfigParsing extends AsyncTask<InputStream, Void, ArrayList<Config>> {

        protected ArrayList<Config> doInBackground(InputStream... streams) {
            XmlPullParserFactory factory;
            XmlPullParser parser;
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newPullParser();

                parser.setInput(streams[0], null);

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {

                        // OPENING TAG
                        case XmlPullParser.START_TAG:
                            if (tagName.equalsIgnoreCase("config")) {
                                config = new Config();
                                config.setAppName(parser.getAttributeValue(null, "app-name"));
                                config.setAppColor(parser.getAttributeValue(null, "app-color"));
                                config.setTabsAmount(Integer.parseInt(parser.getAttributeValue(null, "tabs-amount")));
                                ArrayList<String> tabs = new ArrayList<String>();
                                for (int i = 0; i < config.getTabsAmount(); i++)
                                    tabs.add(i, parser.getAttributeValue(null, "tab" + i));
                                config.setTabs(tabs);
                            }
                            break;

                        // ENCLOSED TEXT
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;

                        // CLOSING TAG
                        case XmlPullParser.END_TAG:
                            if (text != null) {
                                if (tagName.equalsIgnoreCase("config"))
                                    configs.add(config);
                                break;
                            }

                        default:
                            break;
                    }
                    eventType = parser.next();
                }

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return configs;
        }

        protected void onPostExecute(ArrayList<Config> result) {
            configs = result;
        }
    }
}