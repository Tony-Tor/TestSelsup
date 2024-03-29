
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Author: Torochkov Anton Pavlovich
 * Version: 0.3
 * Description: A client for working with the Fair Sign API.
 * Sends a POST to create a new product in the Honest sign API.
 * TimeUnit and the number of requests are passed to the method
 */

public class CrptApi {

    private static final String URI_FAIR_SIGN = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private static final Logger log = Logger.getLogger(CrptApi.class.getName());
    private final TimeUnit timeUnit;
    private final BlockingQueue<Long> requestTimes; //В интернете уже был предоставлен вариант с использованием семафоров и ScheduledExecutorService,
    // поэтому я решил реализовать при помощи очередей по сути тоже самое но немного по другому
    private final HttpClient httpClient;

    /**
     * Создает экземпляр класса реализующий отправку запросов к API честного знака
     *
     * @param timeUnit - временной юнит такой как секунда, минута
     * @param requestLimit - количество запросов в временной юнит
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestTimes = new LinkedBlockingQueue<>(requestLimit); //создаем лимитированную блокирующую очередь
        this.timeUnit = timeUnit;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Инициирует отправку данных на сервер
     * @param document - POJO который будет отправлен на сервер
     * @param signature - сигнатура
     * @throws InterruptedException  - выбрасывает исключение прерывания
     */
    public void createDocument(Document document, String signature) throws InterruptedException {
        log.info("Sending a request");
        long currentTime = System.currentTimeMillis(); //временная метка запроса

        waitFreeCell(currentTime); //Ожидаем
        executeHttpRequest(createJSONFromObject(document), signature); //отправляем
        Thread.sleep(timeUnit.toMillis(1)); //таймаут

        requestTimes.remove(currentTime); //Освобождаем клетку
    }

    /**
     * Приватный метод, который проверяет заполненность очереди, и ожидает в случае переполнения
     * @param currentTime - время когда был вызван метод
     */
    private void waitFreeCell(long currentTime){
        log.info(String.format("A request with a timestamp of %d is waiting to be executed", currentTime));
        boolean canProceed = false;
        while (!canProceed){
            canProceed = requestTimes.offer(currentTime); //заполняет очередь
        }
    }


    /**
     * Преобразует POJO в JSON при помощи fasterxml
     * @param document - POJO объект
     * @return - Возвращает строку
     */
    private String createJSONFromObject(Document document){
        log.info("Create JSON from Document");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = "";
        try {
            json = ow.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            log.severe("JsonProcessingExceptio occurred: " + e.getMessage());
        }

        return json;
    }

    /**
     * Метод выполняющий отправку при помощи HTTPClient и получающий ответ
     * @param jsonDocument - JSON документ для отправки
     * @param signature - сигнатура
     * @throws InterruptedException - выбрасывает исключение прерывания
     */
    private void executeHttpRequest(String jsonDocument, String signature) throws InterruptedException {
        try {
            HttpRequest request = buildHttpRequest(jsonDocument, signature); // генерация запроса на отправку
            log.info("Sending document to API");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); //отправка
            handleResponse(response); //вывод
        } catch (IOException e) {
            log.severe("IOException occurred: " + e.getMessage());
        }
    }

    /**
     * Метод генерирующий запрос к серверу Честный Знак
     * @param jsonDocument - JSON документ для отправки
     * @param signature - сигнатура
     */
    private HttpRequest buildHttpRequest(String jsonDocument, String signature) {
        return HttpRequest.newBuilder()
                .uri(URI.create(URI_FAIR_SIGN))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonDocument)) // Передача тела в POST запросе
                .build();
    }

    /**
     * Метод для получения ответа от сервера
     * @param response - необходимо передать запрос
     */
    private void handleResponse(HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            log.info("Response received successfully");
        } else {
            log.warning("Response with status code: " + response.statusCode());
        }
    }

    /**
     * Тестирование
     * @param args - аргументы метода main
     */
    public static void main(String[] args) {
        // Пример использования класса CrptApi
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 1);

        String json = "{\n" +
                "  \"description\": {\n" +
                "    \"participantInn\": \"string\"\n" +
                "  },\n" +
                "  \"doc_id\": \"string\",\n" +
                "  \"doc_status\": \"string\",\n" +
                "  \"doc_type\": \"LP_INTRODUCE_GOODS\",\n" +
                "  \"importRequest\": true,\n" +
                "  \"owner_inn\": \"string\",\n" +
                "  \"participant_inn\": \"string\",\n" +
                "  \"producer_inn\": \"string\",\n" +
                "  \"production_date\": \"2020-01-23\",\n" +
                "  \"production_type\": \"string\",\n" +
                "  \"products\": [\n" +
                "    {\n" +
                "      \"certificate_document\": \"string\",\n" +
                "      \"certificate_document_date\": \"2020-01-23\",\n" +
                "      \"certificate_document_number\": \"string\",\n" +
                "      \"owner_inn\": \"string\",\n" +
                "      \"producer_inn\": \"string\",\n" +
                "      \"production_date\": \"2020-01-23\",\n" +
                "      \"tnved_code\": \"string\",\n" +
                "      \"uit_code\": \"string\",\n" +
                "      \"uitu_code\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"reg_date\": \"2020-01-23\",\n" +
                "  \"reg_number\": \"string\"\n" +
                "}";

        Document document = new Gson().fromJson(json, Document.class);

        String signature = "exampleSignature";
        for (int i = 0; i<=30;i++)
            new Thread(()->{
                try {
                    crptApi.createDocument(document, signature);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
    }

    @Data
    public static class Description{
        public String participantInn;
    }

    @Data
    public static class Product{
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }

    @Data
    public static class Document{
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public ArrayList<Product> products;
        public String reg_date;
        public String reg_number;
    }
}