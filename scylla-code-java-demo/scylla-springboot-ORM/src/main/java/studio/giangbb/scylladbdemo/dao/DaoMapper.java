package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * Created by giangbb on 12/05/2024
 */
@Mapper
public interface DaoMapper {

    @DaoFactory
    UserDao userDao();

    @DaoFactory
    ClientDao clientDao();

    @DaoFactory
    CarDao carDao();
}