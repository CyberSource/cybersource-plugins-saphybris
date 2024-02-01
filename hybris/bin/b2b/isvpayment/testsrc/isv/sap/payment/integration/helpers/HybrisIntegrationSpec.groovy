package isv.sap.payment.integration.helpers

import java.lang.reflect.Field
import javax.annotation.Resource

import de.hybris.platform.core.Registry
import de.hybris.platform.servicelayer.ServicelayerBaseTest
import org.apache.log4j.Logger
import org.junit.Before
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.util.ReflectionUtils
import spock.lang.Specification

class HybrisIntegrationSpec extends Specification
{
    private static final Logger LOG = Logger.getLogger(ServicelayerBaseTest)

    @Before
    void prepareApplicationContextAndSession() throws Exception
    {
        Registry.setCurrentTenantByID('junit')
        ApplicationContext applicationContext = Registry.applicationContext
        this.autowireProperties(applicationContext)
    }

    protected static String getBeanName(Resource resource, Field field)
    {
        resource.mappedName() != null && resource.mappedName().length() > 0 ? resource.mappedName() : (resource.name() != null && resource.name().length() > 0 ? resource.name() : field.name)
    }

    @SuppressWarnings('BracesForClass')
    protected void autowireProperties(ApplicationContext applicationContext)
    {
        def beanFactory = applicationContext.autowireCapableBeanFactory
        def missing = new LinkedHashSet()
        ReflectionUtils.doWithFields(this.getClass(), new ReflectionUtils.FieldCallback() {
            void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
            {
                Resource resource = (Resource) field.getAnnotation(Resource)
                if (resource != null)
                {
                    field.setAccessible(true)
                    Object bean = ReflectionUtils.getField(field, HybrisIntegrationSpec.this)
                    if (bean == null)
                    {
                        String beanName = getBeanName(resource, field)

                        try
                        {
                            bean = beanFactory.getBean(beanName, field.type)
                            if (bean != null)
                            {
                                ReflectionUtils.setField(field, HybrisIntegrationSpec.this, bean)
                            }
                        }
                        catch (BeansException var6)
                        {
                            LOG.error('error fetching bean ' + beanName + ' : ' + var6.message, var6)
                        }

                        if (bean == null)
                        {
                            missing.add(field.name)
                        }
                    }
                }
            }
        })
        if (!missing.isEmpty())
        {
            throw new IllegalStateException('test ' + this.getClass().simpleName + ' is not properly initialized - missing bean references ' + missing)
        }
    }
}
