package isv.sap.payment.injector;

import jakarta.annotation.Resource;

import com.google.inject.Injector;
import org.springframework.beans.factory.FactoryBean;

import static com.google.inject.Key.get;
import static com.google.inject.name.Names.named;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class GuiceFactoryBean<T> implements FactoryBean<T>
{
    @Resource(name = "isv.sap.payment.guiceInjector")
    private Injector guiceInjector;

    private Class<T> beanClass;

    private String annotatedWith;

    private boolean singleton = true;

    @Override
    public T getObject() throws Exception // NOPMD
    {
        return isNotEmpty(annotatedWith)
                ? guiceInjector.getInstance(get(beanClass, named(annotatedWith)))
                : guiceInjector.getInstance(beanClass);
    }

    @Override
    public Class<?> getObjectType()
    {
        return beanClass;
    }

    @Override
    public boolean isSingleton()
    {
        return singleton;
    }

    public void setSingleton(final boolean singleton)
    {
        this.singleton = singleton;
    }

    
    public void setBeanClass(final Class<T> beanClass)
    {
        this.beanClass = beanClass;
    }

    public void setAnnotatedWith(final String annotatedWith)
    {
        this.annotatedWith = annotatedWith;
    }
}
