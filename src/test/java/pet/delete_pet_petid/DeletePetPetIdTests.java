package pet.delete_pet_petid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import petstore.model.Category;
import petstore.model.Pet;
import petstore.model.Tag;

import java.util.List;

import static io.qameta.allure.Allure.step;

public class DeletePetPetIdTests {
    private static final String URL = "http://localhost:";
    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v3";
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    private String jsonRequestBody;
    private Pet petRequest;
    private String baseUri;
    private String postPet;
    private String deleteUri;
    private String getUri;
    private Long ID = 14L;


    @SneakyThrows
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        baseUri =
                step("Создание базового URI", () ->
                        URL + PORT + BASE_PATH);
        headers = new HttpHeaders();
        step("Создание хедеров", () -> {
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

        postPet =
                step("Создание POST запроса", () ->
                        baseUri + "/pet");
        step("Вызов запроса POST /pet", () ->
                restTemplate.exchange(postPet, HttpMethod.POST, new HttpEntity<>(jsonRequestBody, headers), String.class));
    }

    @Story("DELETE /pet/{petId}")
    @Test(description = "Метод DELETE /pet/{petId} должен удалить созданного питомца по id")
    public void deletePetPetIdMustDeleteCreatedPetByIdTest() {
        deleteUri =
                step("Создание запроса DELETE /pet/{petId}", () ->
                        baseUri + "/pet/" + ID);
        step("Вызов запроса DELETE /pet/{petId}", () ->
                restTemplate.exchange(deleteUri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class));

        getUri =
                step("Создание запроса GET /pet/{petId}", () ->
                        baseUri + "/pet/" + ID);
        HttpClientErrorException exception =
                step("Вызов запроса GET /pet/{petId} по id удалённого питомца", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class, () ->
                                        restTemplate.exchange(getUri, HttpMethod.GET, new HttpEntity<>(headers), Pet.class).getBody())
                );
        step("Сравнение ожидаемого и фактического статус кода запроса GET /pet/{petId}", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND));


    }

    @Story("DELETE /pet/{petId}")
    @Test(description = "Метод DELETE /pet/{petId} должен вернуть статус код Bad Request")
    public void deletePetPetIdShouldReturnStatusCodeBadRequestTest() {
        deleteUri =
                step("Создание запроса DELETE /pet/{petId} со строкой", () ->
                        baseUri + "/pet/string");

        HttpClientErrorException exception =
                step("Вызов запроса DELETE /pet/{petId} со строкой в место id", () ->
                        Assert.expectThrows(
                                HttpClientErrorException.class, () ->
                                        restTemplate.exchange(deleteUri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                        ));
        step("Сравнение ожидаемого и фактического статус кода запроса DELETE /pet/{petId}", () ->
                Assert.assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST));

        System.out.println(exception);
    }


}
