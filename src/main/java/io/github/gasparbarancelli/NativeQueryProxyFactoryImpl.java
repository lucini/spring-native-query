package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInterceptor;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;

public class NativeQueryProxyFactoryImpl implements NativeQueryProxyFactory {

    private final NativeQueryMethodInterceptor nativeQueryMethodInterceptor;

    public NativeQueryProxyFactoryImpl() {
        this.nativeQueryMethodInterceptor = new NativeQueryMethodInterceptorImpl();
    }

    @Override
    public Object create(Class<? extends NativeQuery> nqClass) {
        ProxyFactory proxy = new ProxyFactory();
        proxy.setTarget(Mockito.mock(nqClass));
        proxy.setInterfaces(nqClass, NativeQuery.class);
        proxy.addAdvice((MethodInterceptor) invocation -> {
            if ("toString".equals(invocation.getMethod().getName())) {
                return "NativeQuery Implementation";
            }
            NativeQueryInfo info = NativeQueryInfo.of(nqClass, invocation);
            return nativeQueryMethodInterceptor.executeQuery(info);
        });
        return proxy.getProxy(nqClass.getClassLoader());
    }

}
