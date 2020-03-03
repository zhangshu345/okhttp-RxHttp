package rxhttp;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * User: ljx
 * Date: 2020/3/3
 * Time: 22:04
 */
public interface IRxHttp {

    Call newCall();

    Call newCall(OkHttpClient okHttpClient);
}
