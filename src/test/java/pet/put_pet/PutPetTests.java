package pet.put_pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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

@Feature("Pet")
public class PutPetTests {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private final Long ID = 15L;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUri;
    private String uriPut;
    private String uriPost;
    private String uriGet;
    private String putJsonRequestBody;
    private String postJsonRequestBody;
    private HttpHeaders headers = new HttpHeaders();
    private Pet putPetRequest;
    private Pet postPetRequest;

    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        baseUri =
                step("Создание базового URL", () ->
                        URL + PORT + BASE_PATH);
        uriPost =
                step("Создание URI для запроса POST /pet", () ->
                        baseUri + "/pet");
        uriPut =
                step("Создание URI для запроса PUT /pet", () ->
                        baseUri + "/pet");
        uriGet =
                step("Создание URI для запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/" + ID);

        headers = new HttpHeaders();
        step("Создание хедеров", () -> {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        });

        postPetRequest = new Pet();
        step("Заполнение модели Pet данными", () ->
                postPetRequest
                        .id(ID)
                        .name("Volt")
                        .category(new Category().id(1L).name("Dogs"))
                        .photoUrls(List.of("url1", "url2"))
                        .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                        .status(Pet.StatusEnum.SOLD));

        postJsonRequestBody =
                step("Модель Pet в json", () ->
                        new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(postPetRequest));

        step("Вызов POST /pet запроса для создания питомца", () ->
                restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(postJsonRequestBody, headers), String.class));
    }

    @Story("PUT /pet")
    @Test(description = "Метод PUT /pet должен вернуть статус код Ok")
    public void putPetShouldReturnStatusCodeOkTest() {

        putPetRequest = new Pet();
        step("Изменение данных в модели Pet", () ->
                putPetRequest
                        .id(ID)
                        .name("Voltik")
                        .category(new Category().id(1L).name("Dogiis"))
                        .photoUrls(List.of("url1", "url2"))
                        .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                        .status(Pet.StatusEnum.SOLD));

        putJsonRequestBody =
                step("Модель Pet в json", () ->
                        new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(putPetRequest));

        step("Вызов PUT /pet запроса для внесения изменений в созданного питомца", () ->
                restTemplate.exchange(uriPut, HttpMethod.PUT, new HttpEntity<>(putJsonRequestBody, headers), String.class));

        Pet getPetByPetId =
                step("Вызов запроса GET /pet/{petId} для получения изменённого питомца", () ->
                        restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody());

        SoftAssert softAssert = new SoftAssert();
        step("Сравнение ожидаемого и фактического результата", () -> {
            step("Сравнение по id", () ->
                    softAssert.assertEquals(getPetByPetId.getId(), putPetRequest.getId(),
                            "Поле id не совпадает"));

            step("Сравнение по name", () ->
                    softAssert.assertEquals(getPetByPetId.getName(), putPetRequest.getName(),
                            "Поле name не совпадает"));

            step("Сравнение по category", () ->
                    softAssert.assertEquals(getPetByPetId.getCategory(), putPetRequest.getCategory(),
                            "Поле category не совпадает"));

            step("Сравнение по photoUrls", () ->
                    softAssert.assertEquals(getPetByPetId.getPhotoUrls(), putPetRequest.getPhotoUrls(),
                            "Поле category не совпадает"));

            step("Сравнение по tags", () ->
                    softAssert.assertEquals(getPetByPetId.getTags(), putPetRequest.getTags(),
                            "Поле ctags не совпадает"));

            step("Сравнение по status", () ->
                    softAssert.assertEquals(getPetByPetId.getStatus(), putPetRequest.getStatus(),
                            "Поле status не совпадает"));
            softAssert.assertAll();
        });
    }

    @Story("PUT /pet")
    @Test(description = "Метод PUT /pet должен вернуть статус код Not Found")
    public void putPetShouldReturnStatusCodeNotFoundTest() {

        putPetRequest = new Pet();
        step("Изменение данных в модели Pet", () ->
                putPetRequest
                        .id(16L)
                        .name("Voltik")
                        .category(new Category().id(1L).name("Dogiis"))
                        .photoUrls(List.of("url1", "url2"))
                        .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                        .status(Pet.StatusEnum.SOLD));
        putJsonRequestBody =
                step("Модель Pet в json", () ->
                        new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(putPetRequest));

        HttpClientErrorException exception =
                step("Вызов запроса PUT /pet с другим id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriPut, HttpMethod.PUT, new HttpEntity<>(putJsonRequestBody, headers), String.class))
                );

        step("Сравнение фактического и ожидаемого статус кода PUT /pet запроса", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND));
    }

    @Story("PUT /pet")
    @Test(description = "Метод PUT /pet должен вернуть статус код Bad Request")
    public void putPetShouldReturnStatusCodeBadRequestTest() {
        String putJsonRequestBodyBadRequest = "";

        HttpClientErrorException exception =
                step("Вызов запроса PUT /pet с другим id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriPut, HttpMethod.PUT, new HttpEntity<>(putJsonRequestBodyBadRequest, headers), String.class))
                );

        step("Сравнение фактического и ожидаемого статус кода PUT /pet запроса", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));
    }
}
