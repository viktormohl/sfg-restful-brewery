package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.v2.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class BeerController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final BeerService beerService;

    @GetMapping(produces = { "application/json" }, path = "beer")
    public ResponseEntity<Mono<BeerPagedList>> listBeers(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                   @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                   @RequestParam(value = "beerName", required = false) String beerName,
                                                   @RequestParam(value = "beerStyle", required = false) BeerStyleEnum beerStyle,
                                                   @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand){

        if (showInventoryOnHand == null) {
            showInventoryOnHand = false;
        }

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        BeerPagedList beerList = beerService.listBeers(beerName, beerStyle, PageRequest.of(pageNumber, pageSize), showInventoryOnHand);

        return ResponseEntity.ok(Mono.just(beerList));
    }

    @GetMapping("beer/{beerId}")
    public ResponseEntity<Mono<BeerDto>> getBeerById(
            @PathVariable("beerId") UUID beerId,
            @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand){
        if (showInventoryOnHand == null) {
            showInventoryOnHand = false;
        }

        return ResponseEntity.ok().body(Mono.just(beerService.getById(beerId, showInventoryOnHand)));
    }

    @GetMapping("beerUpc/{upc}")
    public ResponseEntity<Mono<BeerDto>> getBeerByUpc(@PathVariable("upc") String upc){
        return ResponseEntity.ok(Mono.just(beerService.getByUpc(upc)));
    }

    @PostMapping(path = "beer")
    public ResponseEntity<Void> saveNewBeer(@RequestBody @Validated BeerDto beerDto){
        BeerDto savedBeer = beerService.saveNewBeer(beerDto);
        return ResponseEntity
                .created(UriComponentsBuilder
                        .fromHttpUrl("http://api.springframework.guru/api/v1/beer/" + savedBeer.getId().toString())
                        .build().toUri())
                .build();
    }

    @PutMapping("beer/{beerId}")
    public ResponseEntity<BeerDto> updateBeerById(@PathVariable("beerId") UUID beerId, @RequestBody @Validated BeerDto beerDto){
        BeerDto beerDto1 = beerService.updateBeer(beerId, beerDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("beer/{beerId}")
    public ResponseEntity<Void> deleteBeerById(@PathVariable("beerId") UUID beerId){
        try {
            beerService.deleteBeerById(beerId);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException){
            log.debug("Beer id Not Found: {}", beerId.toString());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

}
