package studio.giangbb.scylladbdemo.repository;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by giangbb on 14/06/2023
 */
public class RowCollector extends CompletableFuture<List<Row>>{
    private List<Row> rows = new ArrayList<>();

    private AsyncResultSet first;
    private Long offset;
    private Long limit;

    public RowCollector(AsyncResultSet first, Long offset, Long limit) {
        this.first = first;
        this.offset = offset;
        this.limit = limit;
        consumePage(first);
    }


    private void consumePage(AsyncResultSet page) {
        for (Row row : page.currentPage()) {
            if (offset > 0) {
                offset--;
            } else if (limit > 0) {
                rows.add(row);
                limit--;
            }
        }
        if (page.hasMorePages() && limit > 0) {
            page.fetchNextPage().<AsyncResultSet>thenAccept(nextPage -> consumePage(nextPage));
        } else {
            complete(rows);
        }
    }


}
