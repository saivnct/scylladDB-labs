package studio.giangbb.scylladbdemo.dao;

import com.datastax.oss.driver.internal.core.util.concurrent.LazyReference;
import com.datastax.oss.driver.internal.mapper.DefaultMapperContext;
import java.lang.Override;
import java.lang.SuppressWarnings;

/**
 * Do not instantiate this class directly, use {@link DaoMapperBuilder} instead.
 *
 * <p>Generated by the DataStax driver mapper, do not edit directly.
 */
@SuppressWarnings("all")
public class DaoMapperImpl__MapperGenerated implements DaoMapper {
  private final DefaultMapperContext context;

  private final LazyReference<UserDao> getUserDaoCache;

  public DaoMapperImpl__MapperGenerated(DefaultMapperContext context) {
    this.context = context;
    this.getUserDaoCache = new LazyReference<>(() -> UserDaoImpl__MapperGenerated.init(context));
  }

  @Override
  public UserDao getUserDao() {
    return getUserDaoCache.get();
  }
}