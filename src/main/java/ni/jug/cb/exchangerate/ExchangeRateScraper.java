package ni.jug.cb.exchangerate;

import java.io.IOException;
import java.math.BigDecimal;
import ni.jug.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
interface ExchangeRateScraper {

    String ERROR_FOR_PARSING_TEXT = "No se pudo extraer el dato de [%s]";
    String ERROR_FOR_READING_HTML = "No se pudo extraer el dato, el HTML del sitio web de [%s] ha sido modificado";

    String bank();

    String url();

    ExchangeRateTrade extractData();

    default Document makeGetRequest() {
        try {
            return Jsoup.connect(url())
                    .validateTLSCertificates(false)
                    .get();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
        }
    }

    default Elements selectExchangeRateElements(int expectedMinimumSize, String cssSelector) {
        Document doc = makeGetRequest();
        Elements elements = doc.select(cssSelector);
        if (elements.size() < expectedMinimumSize) {
            throw new IllegalArgumentException(String.format(ERROR_FOR_READING_HTML, bank()));
        }
        return elements;
    }

    default String fetchAsPlainText() {
        try {
            return Jsoup.connect(url())
                    .validateTLSCertificates(false)
                    .ignoreContentType(true)
                    .execute()
                    .body();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("No se pudo obtener el contenido del sitio web de [" + bank() + "]", ioe);
        }
    }

    default ExchangeRateTrade extractDataFromContent(String content, String leftBuy, String rightBuy, String leftSell, String rightSell,
            String offset) {
        String buyText = Strings.substringBetween(content, leftBuy, rightBuy, offset);
        if (buyText.isEmpty()) {
            throwParsingError(content);
        }
        BigDecimal buy = new BigDecimal(buyText).setScale(4);

        String sellText = Strings.substringBetween(content, leftSell, rightSell, leftBuy + buyText + rightBuy);
        if (sellText.isEmpty()) {
            throwParsingError(content);
        }
        BigDecimal sell = new BigDecimal(sellText).setScale(4);

        return new ExchangeRateTrade(bank(), buy, sell);
    }

    default ExchangeRateTrade extractDataFromPlainTextResponse(String leftBuy, String rightBuy, String leftSell, String rightSell,
            String offset) {
        String response = fetchAsPlainText();
        return extractDataFromContent(response, leftBuy, rightBuy, leftSell, rightSell, offset);
    }

    default ExchangeRateTrade extractDataFromPlainTextResponse(String open, String close) {
        return extractDataFromPlainTextResponse(open, close, open, close, null);
    }

    default void throwParsingError(String value) {
        throw new IllegalArgumentException(String.format(ERROR_FOR_PARSING_TEXT, value));
    }

    default BigDecimal parseText(String value, String offset) {
        String exchangeRateText = (offset == null || offset.isEmpty()) ? value : Strings.substringAfter(value, offset);
        if (exchangeRateText.isEmpty()) {
            throwParsingError(value);
        }
        return new BigDecimal(exchangeRateText).setScale(4);
    }

    default BigDecimal parseText(String value) {
        return parseText(value, null);
    }

}
