
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final BlockingQueue<Long> requestTimes;
    private final HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestTimes = new LinkedBlockingQueue<>(requestLimit);
        this.timeUnit = timeUnit;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void createDocument(String document) throws InterruptedException {
        long currentTime = System.currentTimeMillis(); //временная метка запроса

        boolean canProceed = false;
        while (!canProceed){
            canProceed = requestTimes.offer(currentTime);
        }

        try {
            URI uri = URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(document))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(timeUnit.toMillis(1));

        requestTimes.remove(currentTime);
    }

    public static void main(String[] args) {
        // Пример использования класса CrptApi
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 2);
        String document = "{\"description\": { \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\", \"doc_type\": \"LP_INTRODUCE_GOODS\", \"importRequest\": true, \"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\", \"products\": [ { \"certificate_document\": \"string\", \"certificate_document_date\": \"2020-01-23\", \"certificate_document_number\": \"string\", \"owner_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ], \"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}";
        for (int i = 0; i<=30;i++)
            new Thread(()->{
                try {
                    crptApi.createDocument(document);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
    }
}