
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final Integer requestLimit;
    private final HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) throw new IllegalArgumentException("the request limit must be strictly greater than zero");
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
        this.httpClient = HttpClient.newHttpClient();
    }

    synchronized public void createDocument(String document) throws InterruptedException {
        /*System.out.println("вход в метод");
        long currentTime = System.currentTimeMillis(); //получаем текущее время
        boolean canProceed = false; //проверка возможности исполнения
        while (!canProceed) { //может выполняться
            if (requestTimes.remainingCapacity() == 0) { //проверяем есть ли возможность добавить время запроса
                long oldestRequestTime = requestTimes.take(); //берем элемент сверху и удаляем его, если элементов нет то ждем
                if (currentTime - oldestRequestTime < timeUnit.toMillis(1)) { //если дельта времени меньше чем единица тайм юнита
                    System.out.println("wait" + (timeUnit.toMillis(1) - (currentTime - oldestRequestTime)));
                     - (currentTime - oldestRequestTime)); //усыпить поток на время до окончания секунды
                }
            }
            canProceed = requestTimes.offer(currentTime);
        }*/

        Thread.sleep(timeUnit.toMillis(1)/requestLimit);

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
        System.out.println(System.currentTimeMillis()+" end");
    }

    public static void main(String[] args) {
        // Пример использования класса CrptApi
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 2);
        String document = "{\"description\": { \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\", \"doc_type\": \"LP_INTRODUCE_GOODS\", \"importRequest\": true, \"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\", \"products\": [ { \"certificate_document\": \"string\", \"certificate_document_date\": \"2020-01-23\", \"certificate_document_number\": \"string\", \"owner_inn\": \"string\", \"producer_inn\": \"string\", \"production_date\": \"2020-01-23\", \"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ], \"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}";
        System.out.println(System.currentTimeMillis()+" start");
            for (int i = 0; i<=30;i++)
                new Thread(()->{
                    System.out.println("запуск потока");
                    try {
                        crptApi.createDocument(document);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("прерывание");
                    }
                }).start();
    }
}