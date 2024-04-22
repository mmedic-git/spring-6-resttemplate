package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8081";

    // @Autowired
    BeerClient beerClient;

    // @Autowired
    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;



    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder((new MockServerRestTemplateCustomizer()));

    BeerDTO dto;
    String dtoJson;

    @BeforeEach // ZAPAMTI: ovaj kod će se izvesti prije svake testne metode u ovom unit-u
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build(); //bindamo server i pridružujemo server instance, radije nego kao prije, da Spring Context to radi

        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);  //želimo vratiti restTemplate objekt koji je boundan na server
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);

        dto = getBeerDto();   //obzirom da je getBeerDto() već praktički izgenerirao UUID, imam sve što mi treba za get by  .id(UUID.randomUUID())

        dtoJson = objectMapper.writeValueAsString(dto);   // buffer za JSON string koji ćemo primiti nazad


    }

    @Test
    void testDeleteBeer() {

        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withNoContent());  //DELETE ne vraća Content

        // sjeti se da delete metoda ne vraća ništa
        beerClient.deleteBeer(dto.getId());

        server.verify();  //server će verificirati da li je metoda DELETE pozvana i da li je uspješno izvršena (nije pala u exception)

    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {

        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient.listBeers("ALE", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);


    }

    @Test
    void testDeleteNotFound() {

        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withResourceNotFound()); //očekujemo da DELETE fejla sa Resource Not Found. MOCK će nam po narudžbi vratiti NotFound exception

        // kojeg sad očekujemo i lovimo sa AssertThrows
        assertThrows(HttpClientErrorException.class, () -> { beerClient.deleteBeer(dto.getId());
        });

        server.verify();


    }

    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId())).andRespond(withNoContent());

        mockGetOperation();

        BeerDTO responseDTO = beerClient.updateBeer(dto);
        assertThat(responseDTO.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer() {

        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(dto.getId());

        server.expect(method(HttpMethod.POST))
                        .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO responseDTO = beerClient.createBeer(dto);

        assertThat(responseDTO.getId()).isEqualTo(dto.getId());


    }

    @Test
    void testGetById()  {

        mockGetOperation();

        BeerDTO responseDTO = beerClient.getBeerById(dto.getId());  // ako je test uspješan, znači da smo uspješno dobili json, koji se uspješno konvertira u DTO objekt...

        assertThat(responseDTO.getId()).isEqualTo(dto.getId());  // i id-evi dohvačenog responseDTO i "originalnog" DTO objekta su jednaki

    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        //dajemo naredbu Mockito-u da izvrši poziv GET API-ja, da pozove glavni URL, i da server odgovori sa payloadom tipa JSON
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    BeerDTOPageImpl getPage(){
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}