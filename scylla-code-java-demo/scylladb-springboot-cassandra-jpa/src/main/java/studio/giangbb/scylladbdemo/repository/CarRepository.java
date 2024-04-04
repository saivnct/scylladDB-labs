package studio.giangbb.scylladbdemo.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import studio.giangbb.scylladbdemo.models.Car;

import java.util.List;


/**
 * Created by Giangbb on 01/03/2024
 */
@Repository
public interface CarRepository extends CassandraRepository<Car, Car.Key>, CarRepositoryCustom{
    Slice<Car> findAllByYear(int year, Pageable pageable);


    //find by PK - x.key.brand.
    List<Car> findAllByKeyBrand(String brand);
    List<Car> findAllByKey_Brand(String brand);

    //find by PK - using @Query
    @Query("SELECT * FROM car WHERE brand = :brand AND sub_brand = :subBrand")
    List<Car> findAllByPK(@Param("brand") String brand, @Param("subBrand") String subBrand);

}
