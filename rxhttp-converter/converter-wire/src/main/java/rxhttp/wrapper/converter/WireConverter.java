package rxhttp.wrapper.converter;


import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 15:34
 */
public class WireConverter implements IConverter {

    @Override
    public <T> T convert(ResponseBody body, Type type, boolean onResultDecoder) throws IOException {
        if (!(type instanceof Class<?>)) {
            return null;
        }
        Class<?> c = (Class<?>) type;
        if (!Message.class.isAssignableFrom(c)) {
            return null;
        }
        //noinspection unchecked
        ProtoAdapter<T> adapter = ProtoAdapter.get((Class<T>) c);
        try {
            return adapter.decode(body.source());
        } finally {
            body.close();
        }
    }
}
