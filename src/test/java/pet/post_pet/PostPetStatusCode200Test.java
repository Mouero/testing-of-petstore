package pet.post_pet;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import petstore.model.Category;
import petstore.model.Pet;
import petstore.model.Tag;

import java.util.List;

import static io.qameta.allure.Allure.step;

public class PostPetStatusCode200Test {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    private String uriPost;
    private String uriGet;
    private String jsonRequestBody;
    private HttpHeaders headers = new HttpHeaders();
    private Pet petRequest;


    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTestForPostPetTest() {
        String baseUri =
                step("Создание базового URI", () ->
                        URL + PORT + BASE_PATH);
        uriPost =
                step("Создание URI для запроса Post/pet", () ->
                        baseUri + "/pet");
        uriGet =
                step("Создание URI для запроса Get/pet/{petId}", () ->
                        baseUri + "/pet/15");

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


    }

    @Story("Get /pet")
    @Test
    public void PostPetGetPetAssertEqualsTest() {
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

}