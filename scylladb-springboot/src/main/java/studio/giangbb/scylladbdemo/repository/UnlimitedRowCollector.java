package studio.giangbb.scylladbdemo.repository;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by giangbb on 15/06/2023
 */
public class UnlimitedRowCollector extends CompletableFuture<List<Row>> {
    private List<Row> rows = new ArrayList<>();
    private AsyncResultSet first;

    public UnlimitedRowCollector(AsyncResultSet first) {
        this.first = first;
        consumePage(first);
    }

    public void consumePage(AsyncResultSet page) {
        for (Row row : page.currentPage()) {
            rows.add(row);
        }
        if (page.hasMorePages()) {
            page.fetchNextPage().thenAccept(nextPage -> consumePage(nextPage));
        } else {
            complete(rows);
        }
    }
}
