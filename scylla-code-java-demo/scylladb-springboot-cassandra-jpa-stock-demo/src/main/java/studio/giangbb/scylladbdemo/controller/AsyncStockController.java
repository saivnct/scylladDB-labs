package studio.giangbb.scylladbdemo.controller;

/**
 * Created by giangbb on 15/06/2023
 */

import com.datastax.oss.driver.api.core.DriverException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import studio.giangbb.scylladbdemo.common.controller.StockUriHelper;
import studio.giangbb.scylladbdemo.model.Stock;
import studio.giangbb.scylladbdemo.repository.AsyncStockRepository;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.springframework.data.util.Optionals.ifPresentOrElse;

/** A REST controller that performs CRUD actions on [Stock] instances.  */
@CrossOrigin(origins = {"http://localhost:8082"})
@RestController
@RequestMapping("/api/v1")
public class AsyncStockController {
    private final AsyncStockRepository stockRepository;
    private final StockUriHelper uriHelper;


    @Autowired
    public AsyncStockController(AsyncStockRepository stockRepository, StockUriHelper uriHelper) {
        this.stockRepository = stockRepository;
        this.uriHelper = uriHelper;
    }

    /**
     * Creates a new stock value (POST method).
     *
     * @param stock The stock value to create.
     * @param request The current HTTP request.
     * @return The created stocked value.
     */
    @PostMapping("/stocks")
    public CompletionStage<ResponseEntity<Stock>> createStock(@RequestBody Stock stock, @NonNull HttpServletRequest request){
        return stockRepository
                .save(stock)
            .thenApply (createdStock -> {
                URI location = uriHelper.buildDetailsUri(request, createdStock);
                return ResponseEntity.created(location).body(createdStock);
            });
    }

    /**
     * Updates the stock value at the given path (PUT method).
     *
     * @param symbol The stock symbol to update.
     * @param date The stock date to update.
     * @param stock The new stock value.
     * @return The updated stock value.
     */
    @PutMapping("/stocks/{symbol}/{date}")
    public CompletionStage<ResponseEntity<Stock>> updateStock(
            @PathVariable("symbol") String symbol,
            @PathVariable("date") Instant date,
            @RequestBody Stock stock
    ){
        CompletableFuture<ResponseEntity<Stock>> future = new CompletableFuture<ResponseEntity<Stock>>();

        stockRepository
                .findById(symbol, date)
                .whenComplete((optionalStock, error1) -> {
                    if (error1 == null) {
                        optionalStock
                                .map (foundStock -> new Stock(foundStock.getSymbol(), foundStock.getDate(), foundStock.getValue()))
                                .ifPresentOrElse(
                                        toUpdateStock -> {
                                            stockRepository.save(toUpdateStock)
                                                .whenComplete((updatedStock, error2) -> {
                                                    if (error2 == null) {
                                                        future.complete(ResponseEntity.ok(updatedStock));
                                                    } else {
                                                        future.completeExceptionally(error2);
                                                    }
                                                });
                                        },
                                        () -> future.complete(ResponseEntity.notFound().build())
                                );

                    } else {
                        future.completeExceptionally(error1);
                    }
                });
        return future;
    }

    /**
     * Deletes a stock value (DELETE method).
     *
     * @param symbol The stock symbol to delete.
     * @param date The stock date to delete.
     * @return An empty response.
     */
    @DeleteMapping("/stocks/{symbol}/{date}")
    public CompletionStage<ResponseEntity<Void>> deleteStock(
            @PathVariable("symbol") String symbol, @PathVariable("date") Instant date
    ) {
        return stockRepository
                .deleteById(symbol, date)
                .thenApply(res -> ResponseEntity.ok().build());
    }

    /**
     * Retrieves the stock value for the given symbol and date (GET method).
     *
     * @param symbol The stock symbol to find.
     * @param date The stock date to find.
     * @return The found stock value, or empty if no stock value was found.
     */
    @GetMapping("/stocks/{symbol}/{date}")
    public CompletionStage<ResponseEntity<Stock>> findStock(
            @PathVariable("symbol") String symbol,
            @PathVariable("date") Instant date
    ) {
        return stockRepository
                .findById(symbol, date)
                .thenApply(
                        stock -> stock
                                .map(body -> ResponseEntity.ok(body))
                                .orElse(ResponseEntity.notFound().build())
                );
    }

    /**
     * Lists the available stocks for the given symbol and date range (GET method).
     *
     * @param symbol The symbol to list stocks for.
     * @param offset The zero-based index of the first result to return.
     * @param limit The maximum number of results to return.
     * @return The available stocks for the given symbol and date range.
     */
    @GetMapping("/stocks/{symbol}")
    public CompletionStage<Stream<Stock>> listStock(
            @PathVariable(name = "symbol") @NonNull String symbol
    ) {
        return stockRepository.findStockBySymbol(symbol);
    }

    /**
     * Lists the available stocks.
     *
     * @return The available stocks
     */
    @GetMapping("/stocks")
    public CompletionStage<Stream<Stock>> listStocks() {
        return stockRepository.findAll();
    }

    /**
     * Converts [DriverException]s into HTTP 500 error codes and outputs the error message as
     * the response body.
     *
     * @param e The [DriverException].
     * @return The error message to be used as response body.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorHandler(DriverException e) {
        return e.getMessage();
    }
    
}
