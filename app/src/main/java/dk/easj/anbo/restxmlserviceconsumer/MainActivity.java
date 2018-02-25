package dk.easj.anbo.restxmlserviceconsumer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    private static final String NEW_LINE = "\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AccessWebServiceTask task = new AccessWebServiceTask();
        task.execute("Android");
    }

    private InputStream openHttpConnection(final String urlString)
            throws IOException {
        final URL url = new URL(urlString);
        final URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        final HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setAllowUserInteraction(false);
        // No user interaction like dialog boxes, etc.
        httpConn.setInstanceFollowRedirects(true);
        // follow redirects, response code 3xx
        httpConn.setRequestMethod("GET");
        httpConn.connect();
        final int response = httpConn.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            return httpConn.getInputStream();
        } else {
            throw new IOException("HTTP response not OK");
        }
    }

    private String wordDefinition(final String word) throws IOException,
            ParserConfigurationException, SAXException {
        final String urlString =
                "http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + word;
        final InputStream in = openHttpConnection(urlString);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);
        document.getDocumentElement().normalize();
        final NodeList definitionElements = document
                .getElementsByTagName("Definition");
        final StringBuilder strDefinition = new StringBuilder();
        for (int i = 0; i < definitionElements.getLength(); i++) {
            Node itemNode = definitionElements.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element definitionElement = (Element) itemNode;
                NodeList wordDefintionElements =
                        definitionElement.getElementsByTagName("WordDefinition");
                strDefinition.append(NEW_LINE);
                for (int j = 0; j < wordDefintionElements.getLength(); j++) {
                    Element wordDefinitionElement = (Element) wordDefintionElements.item(j);
                    NodeList textNodes = wordDefinitionElement.getChildNodes();
                    strDefinition.append(textNodes.item(0).getNodeValue()).append(". ");
                }
            }
        }
        final String result = strDefinition.toString();
        if ("".equals(result)) {
            return "No definition";
        }
        return result;
    }

    /**
     * AsyncTask<String, Void, String> parameter type, progress type, result
     * type
     */
    private class AccessWebServiceTask extends AsyncTask<String, Void, String> {

        /**
         * Used to perform background computation that can take a long time.
         */
        @Override
        protected String doInBackground(String... urls) {
            try {
                return wordDefinition(urls[0]);
            } catch (Exception ex) {
                Log.e("SHIT", ex.toString());
                cancel(false);
                return ex.toString();
            }
        }

        /**
         * Invoked on the UI thread after the background computation finishes.
         * The result of the background computation is passed to this step as a
         * parameter.
         */
        @Override
        protected void onPostExecute(String result) {
            final TextView textView = findViewById(R.id.text);
            textView.setText(result);

            // http://developer.android.com/training/basics/network-ops/xml.html
            //WebView myWebView = (WebView) findViewById(R.id.webview);
            //myWebView.loadData(result, "text/xml", null);
        }

        @Override
        protected void onCancelled(String message) {
            super.onCancelled(message);
            final TextView textView = findViewById(R.id.text);
            textView.setText(message);
        }
    }

    public void onClickGetDefinition(final View view) {
        final EditText inputField = findViewById(R.id.mainWordEditText);
        final String word = inputField.getText().toString();
        final AccessWebServiceTask task = new AccessWebServiceTask();
        task.execute(word);
    }
}
