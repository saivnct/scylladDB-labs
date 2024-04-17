package studio.giangbb.scylladbdemo.entity.tuple;

import com.giangbb.scylla.core.mapping.Element;
import com.giangbb.scylla.core.mapping.Tuple;

import java.util.Objects;

/**
 * Created by UserTuple on 15/04/2024
 */
@Tuple
public class UserTupleIndex {
    @Element(1)
    private String indexName;
    @Element(0)
    private int index;


    public UserTupleIndex() {
    }

    public UserTupleIndex(String indexName, int index) {
        this.indexName = indexName;
        this.index = index;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTupleIndex userTupleIndex = (UserTupleIndex) o;
        return Objects.equals(indexName, userTupleIndex.indexName) && index == userTupleIndex.index;
    }

    @Override
    public String toString() {
        return "UserTupleIndex{" +
                "indexName='" + indexName + '\'' +
                ", index=" + index +
                '}';
    }
}
