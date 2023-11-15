package pet.post_pet;


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
public class PostPetTests {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
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
        uriPost =
                step("Создание URI для запроса Post/pet", () ->
                        baseUri + "/pet");
        headers = new HttpHeaders();
        step("Создание хедеров", () -> {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        });

    }

    @Story("POST /pet")
    @Test(description = "Метод POST /pet должен вернуть модель Pet ")
    public void postPetShouldReturnStatusCodeOkTest() {

        petRequest = new Pet();
        step("Заполнение модели Pet данными", () ->
                petRequest
                        .id(15L)
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

        uriGet =
                step("Создание URI для запроса Get /pet/{petId}", () ->
                        baseUri + "/pet/15");
        step("Вызов Post запроса", () ->
                restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));
        Pet getPetByPetId =
                step("Вызов запроса Get /pet/{petId} для получения созданного питомца", ()->
                        restTemplate.exchange(uriGet,HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody());

        SoftAssert softAssert = new SoftAssert();
        step("Сравнение ожидаемого и фактического результата", () -> {
            step("Сравнение по id", () ->
                    softAssert.assertEquals(getPetByPetId.getId(), petRequest.getId(),
                            "Поле id не совпадает"));

            step("Сравнение по name", () ->
                    softAssert.assertEquals(getPetByPetId.getName(), petRequest.getName(),
                            "Поле name не совпадает"));

            step("Сравнение по category", () ->
                    softAssert.assertEquals(getPetByPetId.getCategory(),petRequest.getCategory(),
                            "Поле category не совпадает"));

            step("Сравнение по photoUrls", () ->
                    softAssert.assertEquals(getPetByPetId.getPhotoUrls(),petRequest.getPhotoUrls(),
                            "Поле category не совпадает"));

            step("Сравнение по tags", () ->
                    softAssert.assertEquals(getPetByPetId.getTags(),petRequest.getTags(),
                            "Поле ctags не совпадает"));

            step("Сравнение по status", () ->
                    softAssert.assertEquals(getPetByPetId.getStatus(),petRequest.getStatus(),
                            "Поле status не совпадает"));

        });

    }

    @Story("POST /pet")
    @Test(description = "Метод POST /pet должен вернуть bad request")
    public void postPetShouldReturnBadRequestTest() {
        HttpClientErrorException exception =
                step("Вызов запроса POST /pet без тела запроса", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class))
                );
        step("Сравнение фактического и ожидаемого статус кода Post /pet запроса",()->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));

    }


}