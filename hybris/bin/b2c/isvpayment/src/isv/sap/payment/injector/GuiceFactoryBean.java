package isv.sap.payment.injector;

import javax.annotation.Resource;

import com.google.inject.Injector;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import static com.google.inject.Key.get;
import static com.google.inject.name.Names.named;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

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

    @Required
    public void setBeanClass(final Class<T> beanClass)
    {
        this.beanClass = beanClass;
    }

    public void setAnnotatedWith(final String annotatedWith)
    {
        this.annotatedWith = annotatedWith;
    }
}
