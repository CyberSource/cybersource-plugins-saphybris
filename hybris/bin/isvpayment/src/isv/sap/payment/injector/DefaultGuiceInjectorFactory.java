package isv.sap.payment.injector;

import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.springframework.beans.factory.annotation.Required;

import isv.cjl.module.PaymentModule;

public class DefaultGuiceInjectorFactory
{
    private List<Module> modules;

    public Injector createInjectorInstance()
    {
        return Guice.createInjector(overrideModulesRecursively(new PaymentModule()));
    }

    private Module overrideModulesRecursively(final Module baseModule)
    {
        Module parentModule = baseModule;

        for (final Module currentModule : modules)
        {
            parentModule = Modules.override(parentModule).with(currentModule);
        }

        return parentModule;
    }

    @Required
    public void setModules(final List<Module> modules)
    {
        this.modules = modules;
    }
}
