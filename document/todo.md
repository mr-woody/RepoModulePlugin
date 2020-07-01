## Todo

> 使用上需要注意的地方：


 1. （限制）Retrofit.Builder的callFactory方法，被复写主要用于动态BaseUrl修改，需要注意避免外部被复写，不然会导致动态BaseUrl修改失效。
 2. （限制）使用Retrofit.create(Class<T> service)时，传入的service接口类中方法返回值Call使用的是com.okay.http.core.Call而不是retrofit2.Call
 3. `Callback`的回调函数均在主线程执行，如果Call绑定了生命周期触发了`cancel()`方法， UI回调方法均不会执行，如果要监听那些请求被取消了，可以通过`onCompleted(Call<T> call, @Nullable Throwable t)` 回调中 t是否为 `DisposedException`来判断
     

> 后期需要支持的功能：

 1. 需要支持缓存功能+缓存模式