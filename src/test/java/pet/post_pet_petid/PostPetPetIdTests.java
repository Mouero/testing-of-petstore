package pet.post_pet_petid;

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

import java.util.List;

import static io.qameta.allure.Allure.step;

public class PostPetPetIdTests {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    private String jsonRequestBody;
    private String baseUri;
    private Pet petRequest;
    private String uriPostPet;
    private String uriPostPetPetId;
    private String uriGet;
    private long ID = 19L;
    private String changedName = "Jack";
    private String changedStatus = "Pending";

    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        baseUri =
                step("Создание базового URL", () ->
                        URL + PORT + BASE_PATH);

        headers = new HttpHeaders();
        step("создание хедеров", () -> {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        });

        petRequest = new Pet();
        step("Заполнение модели Pet данными", () ->
                petRequest
                        .id(ID)
                        .name("Volt")
                        .category(new Category().id(1L).name("Dogs"))
                        .photoUrls(List.of("url1", "url2"))
                        .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                        .status(Pet.StatusEnum.SOLD));

        jsonRequestBody =
                step("Модель Pet в json", () ->
                        new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(petRequest));

        uriPostPet =
                step("Создание URI для запроса POST /pet", () ->
                        baseUri + "/pet");
        step("Вызов POST /pet запроса", () ->
                restTemplate.exchange(uriPostPet, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));
    }

    @Story("POST /pet/{petId}")
    @Test(description = "Метод POST /pet/{petId} должен изменить существующие поля name и status")
    public void postPetPetIdMustChangeExistingNameAndStatusTest() {
        String petName = changedName;
        String petStatus = Pet.StatusEnum.PENDING.getValue();

        uriPostPetPetId =
                step("Создание URI для запроса POST /pet/{petId}", () ->
                        baseUri + "/pet/"+ ID +"?name=%s&status=%s".formatted(petName, petStatus));
        step("Вызов POST /pet/{petId} запроса", () ->
                restTemplate.exchange(uriPostPetPetId, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));

        uriGet =
                step("Создание URI для запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/"+ ID);
        Pet getPetPetId =
                step("Вызов GET /pet/{petId} запроса", () ->
                        restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody());

        SoftAssert softAssert = new SoftAssert();
        step("Сравнение ожидаемого и фактического результата", () -> {
            step("Сравнение по name", () ->
                    softAssert.assertEquals(getPetPetId.getName(), petName,
                            "Поле name не совпадает"));
            step("Сравнение по status", () ->
                    softAssert.assertEquals(getPetPetId.getStatus().getValue(), petStatus,
                            "Поле status не совпадает"));
            softAssert.assertAll();
        });


    }

    @Story("POST /pet/{petId}")
    @Test(description = "Метод POST /pet/{petId} должен вернуть статус код Not Found")
    public void postPetPetIdShouldReturnStatusCodeNotFoundTest() {
        uriPostPetPetId =
                step("Создание URI для запроса POST /pet/{petId} с несуществующим id", () ->
                        baseUri + "/pet/18?name="+ changedName + "&status=" + changedStatus);

        HttpClientErrorException exception =
                step("Вызов запроса POST /pet/{petId} с несуществующим id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class, () ->
                restTemplate.exchange(uriPostPetPetId, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class)));
        step("Сравнение фактического и ожидаемого статус кода Post /pet/{petId} запроса", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND));
    }

    @Story("POST /pet/{petId}")
    @Test(description = "Метод POST /pet/{petId} должен вернуть статус код Bad Request")
    public void postPetPetIdShouldReturnStatusCodeBadRequestTest() {
        uriPostPetPetId =
                step("Создание URI для запроса POST /pet/{petId} без параметров", () ->
                        baseUri + "/pet/" + ID);

        HttpClientErrorException exception =
                step("Вызов запроса POST /pet/{petId} без параметров", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class, () ->
                                        restTemplate.exchange(uriPostPetPetId, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class)));
        step("Сравнение фактического и ожидаемого статус кода Post /pet/{petId} запроса", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));
    }


}
