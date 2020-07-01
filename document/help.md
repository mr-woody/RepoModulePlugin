## 技术点整理

* 怎么设置retrofit动态管理和修改BaseUrl?

```
网上的做法很多，比如：配置多个retrofit对象，定制注解@Headers+Okhttp拦截器 Interceptor 的 intercept(Chain chain)方法里做拦截

但是都不是特别满意，我采用基于okhttps.Call.Factory的解决方案，也就是自定义CallFactory
研究retrofit的源代码我们知道，retrofit最终发起请求是从OkHttpCall里面，createRawCall 方法创建最终的okhttp3.Call对象

private okhttp3.Call createRawCall() throws IOException {
  okhttp3.Call call = callFactory.newCall(requestFactory.create(args));
  if (call == null) {
    throw new NullPointerException("Call.Factory returned null.");
  }
  return call;
}

而callFactory，是Retrofit这个类中通过client和callFactory方法，传递进去的，如下图

/**
 * The HTTP client used for requests.
 * <p>
 * This is a convenience method for calling {@link #callFactory}.
 */
public Builder client(OkHttpClient client) {
  return callFactory(checkNotNull(client, "client == null"));
}

/**
 * Specify a custom call factory for creating {@link Call} instances.
 * <p>
 * Note: Calling {@link #client} automatically sets this value.
 */
public Builder callFactory(okhttp3.Call.Factory factory) {
  this.callFactory = checkNotNull(factory, "factory == null");
  return this;
}

根据上面得知，我们只需要重新定义一个okhttp3.Call.Factory，然后重写newCall(Request request),这样就可以做到替换Request中url的目的

@Override
public final Call newCall(Request request) {
    /*
     * @Headers("BaseUrlName:xxx") for method, or
     * method(@Header("BaseUrlName") String name) for parameter
     */
    String baseUrlName = request.header(BASE_URL_NAME);
    if (baseUrlName != null) {
        okhttp3.HttpUrl newHttpUrl = getNewUrl(baseUrlName, request);
        if (newHttpUrl != null) {
            Request newRequest = request.newBuilder().url(newHttpUrl).build();
            return delegate.newCall(newRequest);
        } else {
            Log.w(TAG, "getNewUrl() return null when baseUrlName==" + baseUrlName);
        }
    }
    return delegate.newCall(request);
}

```
* 怎么让lifecycle结合retrofit，将http请求和Activity或Fragment的生命周期相结合？

```

通用的做法是自定义CallAdapter.Factory，可以返回我们想要的自定义Call，在Call接口添加bind方法于Lifecycle相关联


```

* 怎么扩展 retrofit 的注解类型？

```

通过查看Retrofit源码知道，假如要是在Retrofit自定义的接口上，加上自定义的注解，那么需要考虑三个问题：
1. Retrofit 是如何处理注解
2. Retrofit 调用 OkHttp 的时候都传递了什么参数
3. OkHttp 如何在拦截器中获取到 Retrofit Method 中注解

每次使用Retrofit请求网络的时候，都需要调用 create 方法，来为Retrofit自定义的接口 添加动态代理，每次调用 接口上对应的方法时 都会调用 HttpServiceMethod 的 invoke 方法：


public <T> T create(final Class<T> service) {
    ...
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          ...
            return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
          }
        });
  }

 ServiceMethod<?> loadServiceMethod(Method method) {
    ServiceMethod<?> result = serviceMethodCache.get(method);
    if (result != null) return result;

    synchronized (serviceMethodCache) {
      result = serviceMethodCache.get(method);
      if (result == null) {
        result = ServiceMethod.parseAnnotations(this, method);
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }

可以看到 Retrofit 每次调用 metheod 的时候都会把 method 添加到这个缓存中:

 public final class Retrofit {
   private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();
 }

key 就是 method，value 是 HttpServiceMethod 再来查看它是何方神圣：

  HttpServiceMethod(RequestFactory requestFactory, okhttp3.Call.Factory callFactory,
      Converter<ResponseBody, ResponseT> responseConverter) {
    this.requestFactory = requestFactory;
    this.callFactory = callFactory;
    this.responseConverter = responseConverter;
  }

可以看到它有三个字段：

requestFactory：保存了所有的请求相关的数据，比如请求方法是 GET 还是 POST，url 以及请求参数等。
callFactory：创建 OkHttp 的 Call，用于请求网络
responseConverter： 用于序列化 response

在 requestFactory 中，没法修改 Retrofit 的源码来额外解析自定义的注解。

继续往下看callFactory相关的源码：
  
  private okhttp3.Call createRawCall() throws IOException {
    okhttp3.Call call = callFactory.newCall(requestFactory.create(args));
    ...
    return call;
  }

其中 requestFactory 创建了 okhttp3.Request 也就是在拦截器中可以获取到的 Request:

  okhttp3.Request create(Object[] args) throws IOException {
   ...

    return requestBuilder.get()
        .tag(Invocation.class, new Invocation(method, argumentList))
        .build();
  }

在创建 Request 的时候发现 Retrofit 为 Request 添加了一个 tag，tag 的 value 中包含了所调用的 method ，有了它就可以获取其上面的注解。


```