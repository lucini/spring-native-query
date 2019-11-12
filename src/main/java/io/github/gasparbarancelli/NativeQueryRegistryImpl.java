package io.github.gasparbarancelli;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.beans.Introspector;
import java.util.Set;

public class NativeQueryRegistryImpl implements NativeQueryRegistry {

    private final NativeQueryProxyFactory nativeQueryProxyFactory;

    private BeanDefinitionRegistry registry;

    public NativeQueryRegistryImpl(BeanDefinitionRegistry registry) {
        this.nativeQueryProxyFactory = new NativeQueryProxyFactoryImpl();
        this.registry = registry;
    }

    @Override
    public void registry(Set<Class<? extends NativeQuery>> nimitzNativeQueryList) {
        for (Class<? extends NativeQuery> nqClass : nimitzNativeQueryList) {
            Object source = nativeQueryProxyFactory.create(nqClass);
            AbstractBeanDefinition beanDefinition = NativeQueryBeanDefinition.of(nqClass, source);
            String beanName = Introspector.decapitalize(nqClass.getSimpleName());
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

}
