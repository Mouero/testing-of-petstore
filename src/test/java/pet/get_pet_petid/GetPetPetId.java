package pet.get_pet_petid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import petstore.model.Category;
import petstore.model.Pet;
import petstore.model.Tag;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static io.qameta.allure.Allure.step;

public class GetPetPetId {

    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    private String jsonRequestBody;
    private String baseUri;
    private Pet petRequest;
    private String uriPost;
    private String uriGet;
    private SoftAssert softAssert = new SoftAssert();


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
        uriPost =
                step("Создание URI для запроса POST /pet", () ->
                        baseUri + "/pet");

        step("Вызов POST запроса", () ->
                restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));
    }


    @Story("GET /pet/{petId}")
    @Test(description = "Метод GET /pet/{petId} должен вернуть модель Pet")
    public void getPetPetIdShouldReturnPetTest() {
        uriGet =
                step("Создание URI для запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/15");
        Pet getPetByPetId =
                step("Вызов GET /pet/{petId} запроса", () ->
                        restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody());

        step("Сравнение ожидаемого и фактического регультата", () -> {
            step("Сравнение по полю id", () ->
                    softAssert.assertEquals(getPetByPetId.getId(), petRequest.getId(), "Поле id не совпадает"));
            step("Сравнение по полю name", () ->
                    softAssert.assertEquals(getPetByPetId.getName(), petRequest.getName(), "Поле name не совпадает"));
            step("Сравнение по полю category", () ->
                    softAssert.assertEquals(getPetByPetId.getCategory(), petRequest.getCategory(), "Поле category не совпадает"));
            step("Сравнение по полю photoUrls", () ->
                    softAssert.assertEquals(getPetByPetId.getPhotoUrls(), petRequest.getPhotoUrls(), "Поле photoUrls не совпадает"));
            step("Сравнение по полю tags", () ->
                    softAssert.assertEquals(getPetByPetId.getTags(), petRequest.getTags(), "Поле tags не совпадает"));
            step("Сравнение по полю status", () ->
                    softAssert.assertEquals(getPetByPetId.getStatus(), petRequest.getStatus(), "Поле status не совпадает"));

            softAssert.assertAll();
        });
    }

    @Story("GET /pet/{petId}")
    @Test(description = "Метод GET /pet/{petId} должен вернуть статус код Not Found ")
    public void getPetPetIdShouldReturnStatusCodeNotFoundTest() {
        uriGet =
                step("Создание URI для запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/16");
        HttpClientErrorException exception =
                step("Вызов GET /pet/{petId} запроса с несуществующим id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody())
                );

        step("Сравнение фактического и ожидаемого статус кода GET /pet/{petId} запроса", () ->
                softAssert.assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND));
    }

    @Story("GET /pet/{petId}")
    @Test(description = "Метод GET /pet/{petId} должен вернуть статус код Bad Request ")
    public void getPetPetIdShouldReturnStatusCodeBadRequestTest() {
        uriGet =
                step("Создание URI для запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/string");
        HttpClientErrorException exception =
                step("Вызов GET /pet/{petId} запроса со строкой вместо id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class,
                                () -> restTemplate.exchange(uriGet, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody())
                );

        step("Сравнение фактического и ожидаемого статус кода GET /pet/{petId} запроса", () ->
                softAssert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));
    }

}
