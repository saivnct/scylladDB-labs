package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import studio.giangbb.scylladbdemo.entity.Car;

/**
 * Created by Giangbb on 04/04/2024
 */
@Dao
public interface CarDao extends BaseDao<Car>{
    @Select
    Car findByPrimKey(String brand, String subBrand, int year, String model);

    @Select
    PagingIterable<Car> findByPartKey(String brand, String subBrand);

    @Query("SELECT count(*) FROM car")
    long countAll();

    @Query(value = "TRUNCATE car")
    void deleteAll();

    @Select(customWhereClause = "year = :year")
    PagingIterable<Car> getByYear(int year);
}
