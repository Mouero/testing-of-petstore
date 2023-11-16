package pet.put_pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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

@Feature("Pet")
public class PutPetTests {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUri;
    private String uriPut;
    private String uriPost;
    private String uriGet;
    private String postJsonRequestBody;
    private String putJsonRequestBody;
    private HttpHeaders headers = new HttpHeaders();
    private Pet postPetRequest;
    private Pet putPetRequest;

    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        baseUri =
                step("Создание базового URL", () ->
                        URL + PORT + BASE_PATH);
//        ----------------------------------------------
        uriPut =
                step("Создание URI для запроса Put /pet", () ->
                        baseUri + "/pet");
//        ----------------------------------------------
        uriPost =
                step("Создание URI для запроса Post /pet", () ->
                        baseUri + "/pet");
        headers = new HttpHeaders();
        step("Создание хедеров", () -> {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            postPetRequest = new Pet();
            step("Заполнение модели Pet данными", () ->
                    postPetRequest
                            .id(15L)
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
            step("Вызов Post запроса", () ->
                    restTemplate.exchange(uriPost, HttpMethod.POST, new HttpEntity<>(postJsonRequestBody, headers), String.class));
        });
    }

    @Story("PUT /pet")
    @Test(description = "Метод PUT /pet должен вернуть статус код Ok")
    public void putPetShouldReturnStatusCodeOkTest() {

        putPetRequest = new Pet();
        step("Изменение модели Pet", () ->
                putPetRequest
                        .id(17L)
                        .name("Voltik")
                        .category(new Category().id(1L).name("Dogs"))
                        .photoUrls(List.of("url1", "url2"))
                        .tags(List.of(new Tag().id(0L).name("Crossbreed"), new Tag().id(1L).name("Boy")))
                        .status(Pet.StatusEnum.SOLD));
        putJsonRequestBody =
                step("Модель Pet в json", () ->
                        new ObjectMapper()
                                .writer()
                                .withDefaultPrettyPrinter()
                                .writeValueAsString(putPetRequest));

        step("Вызов Put запроса", () ->
                restTemplate.exchange(uriPut, HttpMethod.PUT, new HttpEntity<>(putJsonRequestBody, headers), String.class));

        uriGet =
                step("Создание URI для запроса Get /pet/{petId}", () ->
                        baseUri + "/pet/15");

        Pet getPetByPetId =
                step("Вызов запроса Get /pet/{petId} для получения созданного питомца", () ->
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


        });


    }
}
