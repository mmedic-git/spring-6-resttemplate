package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient {

    private final RestTemplateBuilder restTemplateBuilder;

    // private static final String BASE_URL = "http://localhost:8081";

    public static final String GET_BEER_PATH = "/api/v1/beer";

    public static final String GET_BEER_BY_ID_PATH = "/api/v1/beer/{beerId}";

    @Override
    public Page<BeerDTO> listBeers() {
        return this.listBeers(null, null, null, null, null);
    }

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber,
                                   Integer pageSize) {

        RestTemplate restTemplate = restTemplateBuilder.build();


        /*
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = BASE_URL + GET_BEER_PATH ;

        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl , String.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);


        // ovaj response će vratiti Map response koristeći Jackson za parsiranje JSON objekata u Java Map

        ResponseEntity<Map> mapResponse
                = restTemplate.getForEntity(fooResourceUrl, Map.class);

        ResponseEntity<JsonNode> jsonNodeResponse =
                restTemplate.getForEntity(fooResourceUrl, JsonNode.class);


        jsonNodeResponse.getBody().findPath("content")
                .elements().forEachRemaining(node -> {
                    System.out.println(node.get("beerName").asText());
                });



         */

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_BEER_PATH);

        if (beerName != null) {
            uriComponentsBuilder.queryParam("beerName", beerName);
        }

        if (beerStyle != null) {
            uriComponentsBuilder.queryParam("beerStyle", beerStyle);
        }

        if (showInventory != null) {
            uriComponentsBuilder.queryParam("showInventory", beerStyle);
        }

        if (pageNumber != null) {
            uriComponentsBuilder.queryParam("pageNumber", beerStyle);
        }

        if (pageSize != null) {
            uriComponentsBuilder.queryParam("pageSize", beerStyle);
        }


        ResponseEntity<BeerDTOPageImpl> response =
                restTemplate.getForEntity(uriComponentsBuilder.toUriString() , BeerDTOPageImpl.class);


        return response.getBody();
    }

    @Override
    public BeerDTO getBeerById(UUID beerId) {

        RestTemplate restTemplate = restTemplateBuilder.build();

        return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
    }

    @Override
    public BeerDTO createBeer(BeerDTO newDto) {

        RestTemplate restTemplate = restTemplateBuilder.build();

        // ResponseEntity<BeerDTO> response = restTemplate.postForEntity(GET_BEER_PATH, newDto, BeerDTO.class);

        URI uri = restTemplate.postForLocation(GET_BEER_PATH, newDto);   // neki API vraćaju cijeli postani objekt, koristeći postForEntity, no neki možda moderniji vraćaju samo uri na novokreirani objekt, pa u tom slučaju koristimo postForLocation


        return restTemplate.getForObject(uri.getPath(), BeerDTO.class); //na ovaj način možemo vratiti URI na objekt koji je nastao izvršavanjem metode pozivatelju/konzumeru metode
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(GET_BEER_BY_ID_PATH, beerDTO, beerDTO.getId());

        return getBeerById(beerDTO.getId());

    }

    @Override
    public void deleteBeer(UUID beerId) {

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(GET_BEER_BY_ID_PATH, beerId);
    }
}
