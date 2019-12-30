package rxhttp.wrapper.converter;


import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.utils.GsonUtil;

/**
 * User: ljx
 * Date: 2019-11-21
 * Time: 22:19
 */
public class GsonConverter implements IConverter {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Gson gson;

    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonConverter create() {
        return create(GsonUtil.buildGson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    // Guarding public API nullability.
    public static GsonConverter create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new GsonConverter(gson);
    }

    private GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @NonNull
    @Override
    public <T> T convert(ResponseBody body, @NonNull Type type, boolean onResultDecoder) throws IOException {
        TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(type);
        TypeAdapter<T> adapter = gson.getAdapter(typeToken);
        JsonReader jsonReader = gson.newJsonReader(body.charStream());

        try {
            T result = adapter.read(jsonReader);
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
            return result;
        } finally {
            body.close();
        }
    }

    @Override
    public <T> RequestBody convert(T value) throws IOException {
        TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(value.getClass());
        TypeAdapter<T> adapter = this.gson.getAdapter(typeToken);
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter jsonWriter = gson.newJsonWriter(writer);
        adapter.write(jsonWriter, value);
        jsonWriter.close();
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
}
