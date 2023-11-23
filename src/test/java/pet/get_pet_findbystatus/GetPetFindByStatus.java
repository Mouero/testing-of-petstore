package pet.get_pet_findbystatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import petstore.model.Category;
import petstore.model.Pet;
import petstore.model.Tag;

import java.util.Arrays;
import java.util.List;

import static io.qameta.allure.Allure.step;

public class GetPetFindByStatus {
    private static String URL = "http://localhost:";
    private static int PORT = 8080;
    private static String BASE_PATH = "/api/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUri;
    private String uriPost;
    private String uriGet;
    private String jsonRequestBody;
    private HttpHeaders headers = new HttpHeaders();
    private Pet petRequest;

    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        baseUri =
                step("Создание базового URL", () ->
                        URL + PORT + BASE_PATH);

        headers = new HttpHeaders();
        step("Создание хедеров", () -> {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        });

            petRequest = new Pet();
            step("Заполнение модели Pet данными", () ->
                    petRequest
                            .id(16L)
                            .name("Volt")
                            .category(new Category().id(1L).name("Dogs"))
                            .photoUrls(List.of("url1", "url2"))
                            .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                            .status(Pet.StatusEnum.SOLD));

            jsonRequestBody = step("Модель Pet в json", () ->
                    new ObjectMapper()
                            .writer()
                            .withDefaultPrettyPrinter()
                            .writeValueAsString(petRequest));
            uriPost =
                    step("Создание URI для запроса Post/pet", () ->
                            baseUri + "/pet");

            step("Вызов Post запроса", () ->
                    restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));



    }

    @Story("GET /pet/findByStatus")
    @Test(description = "Метод GET /pet/findByStatus должен вернуть массив объектов Pet ")
    public void getPetFindByStatusShouldReturnArrayOfPetObjectsTest() {

        uriGet =
                step("Создание URI для запроса GET /pet/findByStatus", () ->
                        baseUri + "/pet/findByStatus?status=sold");

        Pet[] getPetByPetId =
                step("Вызов запроса GET /pet/findByStatus для получения статусов созданных питомцев", () ->
                        restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet[].class).getBody());

                Pet pet1 = Arrays.stream(getPetByPetId)
                .filter(pet -> pet.getStatus().equals(Pet.StatusEnum.SOLD))
                .findFirst()
                .get();

        SoftAssert softAssert = new SoftAssert();
        step("Сравнение ожидаемого и фактического результата", () -> {
            step("Сравнение статуса первого полученного питомца из массива объектов и созданного питомца", () ->
                    softAssert.assertEquals(pet1.getStatus(), petRequest.getStatus()));
        });

    }

    @Story("GET /pet/findByStatus")
    @Test(description = "Метод GET /pet/findByStatus должен вернуть статус код bad request")
    public void getPetFindByStatusShouldReturnStatusCodeBadRequestTest() {

        uriGet =
                step("Создание URI для запроса GET /pet/findByStatus с невалидным статусом питомца", () ->
                        baseUri + "/pet/findByStatus?status=delivery");

        HttpClientErrorException exception =
                step("Вызов запроса GET /pet/findByStatus без статуса", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody())
                );

        step("Сравнение фактического и ожидаемого статус кода GET /pet/findByStatus запроса", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));
    }

}





















