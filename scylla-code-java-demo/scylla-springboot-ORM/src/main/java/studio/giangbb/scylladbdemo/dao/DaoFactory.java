package studio.giangbb.scylladbdemo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by giangbb on 12/05/2024
 */
@Component
public class DaoFactory {
    private final Logger logger = LoggerFactory.getLogger(DaoFactory.class);
    private final DaoMapper daoMapper;

    @Autowired
    public DaoFactory(DaoMapper daoMapper) {
        this.daoMapper = daoMapper;
    }

    public <T extends BaseDao, E> T getDao(Class<E> entityClass) {
        try {
            String simpleName = entityClass.getSimpleName();
            String methodName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1) + "Dao";
            logger.info("Getting DAO methodName: " + methodName);

            Method method = DaoMapper.class.getMethod(methodName);
            return (T) method.invoke(daoMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get DAO of entiry: " + entityClass, e);
        }
    }
}
