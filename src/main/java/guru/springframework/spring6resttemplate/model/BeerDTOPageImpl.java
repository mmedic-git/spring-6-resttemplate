package guru.springframework.spring6resttemplate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;



import java.util.List;

/**
 *
 obzirom da Jackson ne zna sam po sebi kako odhendlati
 ResponseEntity<PageImpl> mapResponse = restTemplate.getForEntity(BASE_URL + GET_BEER_PATH, PageImpl.class);
 provajdamo našu custom implementaciju PageImp klase

 */


// izgleda da Jackson ima neke probleme sa PageImpl<T> generic-som, pa onda predajemo objekt BeerDTO
@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable") //property "pageable" nije prisutan u našem json-u, pa ga nećemo ni tražiti u json-u
public class BeerDTOPageImpl<BeerDTO> extends PageImpl<guru.springframework.spring6resttemplate.model.BeerDTO> {




    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerDTOPageImpl(@JsonProperty("content") List<guru.springframework.spring6resttemplate.model.BeerDTO> content,
                           @JsonProperty("number") int page,
                           @JsonProperty("size") int size,
                           @JsonProperty("totalElements") long total) {




        super(content, PageRequest.of(page, size), total);


        //ovaj kod je unreachable, jer trenutno puca u Constructor-u
        /*
        System.out.println(content.get(0).getBeerName());

        ObjectMapper mapper =new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

         */



    }



    public BeerDTOPageImpl(List<guru.springframework.spring6resttemplate.model.BeerDTO> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BeerDTOPageImpl(List<guru.springframework.spring6resttemplate.model.BeerDTO> content) {
        super(content);
    }
}