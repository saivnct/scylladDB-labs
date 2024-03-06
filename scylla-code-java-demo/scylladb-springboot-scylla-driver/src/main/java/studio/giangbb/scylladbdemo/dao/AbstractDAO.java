package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.internal.mapper.processor.entity.DefaultEntityFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Giangbb on 04/03/2024
 */
public class AbstractDAO<KeyType, T> {
    public static final Logger logger = LogManager.getLogger(AbstractDAO.class);

    protected Class<KeyType> keyTypeClass;
    protected Class<T> tClass;

    protected BaseDao<T> baseDao;


    public AbstractDAO(Class<KeyType> keyTypeClass, Class<T> tClass, BaseDao<T> baseDao) {
        this.keyTypeClass = keyTypeClass;
        this.tClass = tClass;
        this.baseDao = baseDao;
    }


}
