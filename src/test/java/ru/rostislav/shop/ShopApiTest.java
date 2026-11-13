package ru.rostislav.shop;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет контракт MVC, транзакции и фактическую запись в H2. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:shop-test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ShopApiTest {
    private static final String PRODUCTS = "/api/products";
    private static final String ORDERS = "/api/orders";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void catalogSupportsSearchReplacementPartialUpdateAndArchive() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Канцелярия"));

        String product = createProduct("Тетрадь в клетку", "52.90", 6);

        mvc.perform(get(PRODUCTS).param("q", "  КЛЕТКУ "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Тетрадь в клетку"));

        mvc.perform(put(product).contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Тетрадь в линейку", "65.00", 10, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(65.0))
                .andExpect(jsonPath("$.category.id").value(2));

        mvc.perform(patch(product).contentType(MediaType.APPLICATION_JSON).content("{\"stock\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3))
                .andExpect(jsonPath("$.name").value("Тетрадь в линейку"))
                .andExpect(jsonPath("$.price").value(65.0));

        mvc.perform(delete(product)).andExpect(status().isNoContent());
        mvc.perform(delete(product)).andExpect(status().isNoContent());
        mvc.perform(get(product)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(get(PRODUCTS).param("q", "в линейку"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void orderMergesDuplicatesAndPreservesNameAndPriceAfterCatalogChanges() throws Exception {
        String product = createProduct("Ручка синяя", "95.50", 8);
        long id = idFrom(product);
        String order = mvc.perform(post(ORDERS).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(itemJson(id, 1) + "," + itemJson(id, 2))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.total").value(286.5))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(product)).andExpect(jsonPath("$.stock").value(5));
        mvc.perform(put(product).contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Ручка чёрная", "110.00", 5, 1)))
                .andExpect(status().isOk());
        mvc.perform(delete(product)).andExpect(status().isNoContent());

        mvc.perform(get(order))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Ручка синяя"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(95.5))
                .andExpect(jsonPath("$.total").value(286.5));
    }

    @Test
    void stockFailureRollsBackEarlierDeduction() throws Exception {
        String available = createProduct("Папка", "100.00", 10);
        String scarce = createProduct("Ластик", "35.00", 1);
        mvc.perform(post(ORDERS).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(itemJson(idFrom(scarce), 2) + "," + itemJson(idFrom(available), 2))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(header().doesNotExist("Location"));

        mvc.perform(get(available)).andExpect(jsonPath("$.stock").value(10));
        mvc.perform(get(scarce)).andExpect(jsonPath("$.stock").value(1));
    }

    @Test
    void concurrentOrdersCannotBothBuyTheLastUnit() throws Exception {
        String product = createProduct("Последний экземпляр", "30.00", 1);
        Callable<Integer> buy = () -> mvc.perform(post(ORDERS).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(itemJson(idFrom(product), 1))))
                .andReturn().getResponse().getStatus();

        var executor = Executors.newFixedThreadPool(2);
        try {
            var results = executor.invokeAll(List.of(buy, buy), 10, TimeUnit.SECONDS);
            assertThat(List.of(results.get(0).get(), results.get(1).get())).containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
        }
        mvc.perform(get(product)).andExpect(jsonPath("$.stock").value(0));
    }

    @Test
    void invalidInputGets400WithUsefulErrorCode() throws Exception {
        String product = createProduct("Маркер", "42.00", 3);

        mvc.perform(patch(product).contentType(MediaType.APPLICATION_JSON).content("{\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.length()").value(1));
        mvc.perform(patch(product).contentType(MediaType.APPLICATION_JSON).content("{\"stock\":1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
        mvc.perform(post(ORDERS).contentType(MediaType.APPLICATION_JSON).content(orderJson("null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get(PRODUCTS + "/0")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mvc.perform(get(product)).andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void missingCategoryAndArchivedOrderProductGet404() throws Exception {
        mvc.perform(post(PRODUCTS).contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Карандаш", "12.00", 5, Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        String product = createProduct("Архивный товар", "12.00", 5);
        mvc.perform(delete(product)).andExpect(status().isNoContent());
        mvc.perform(post(ORDERS).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(itemJson(idFrom(product), 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(get(ORDERS + "/" + Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private String createProduct(String name, String price, int stock) throws Exception {
        var response = mvc.perform(post(PRODUCTS).contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(name, price, stock, 1)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse();
        long id = json.readTree(response.getContentAsString()).get("id").asLong();
        assertThat(response.getHeader("Location")).isEqualTo(PRODUCTS + "/" + id);
        return response.getHeader("Location");
    }

    private String productJson(String name, String price, int stock, long category) {
        return """
                {"name":"%s","price":%s,"stock":%d,"categoryId":%d}
                """.formatted(name, price, stock, category);
    }

    private String orderJson(String items) {
        return """
                {"customerName":"Анна","customerEmail":"anna@example.test","items":[%s]}
                """.formatted(items);
    }

    private String itemJson(long id, int quantity) {
        return "{\"productId\":%d,\"quantity\":%d}".formatted(id, quantity);
    }

    private long idFrom(String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
