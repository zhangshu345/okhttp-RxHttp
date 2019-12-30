package rxhttp.wrapper.converter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ByteString;
import rxhttp.wrapper.callback.IConverter;

/**
 * User: ljx
 * Date: 2019-11-24
 * Time: 15:34
 */
public class MoshiConverter implements IConverter {

    private static final ByteString UTF8_BOM = ByteString.decodeHex("EFBBBF");

    private final Moshi moshi;
    private final boolean lenient;
    private final boolean failOnUnknown;
    private final boolean serializeNulls;

    private MoshiConverter(Moshi moshi, boolean lenient, boolean failOnUnknown, boolean serializeNulls) {
        this.moshi = moshi;
        this.lenient = lenient;
        this.failOnUnknown = failOnUnknown;
        this.serializeNulls = serializeNulls;
    }

    /**
     * Create an instance using a default {@link Moshi} instance for conversion.
     */
    public static MoshiConverter create() {
        return create(new Moshi.Builder().build());
    }

    /**
     * Create an instance using {@code moshi} for conversion.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static MoshiConverter create(Moshi moshi) {
        if (moshi == null) throw new NullPointerException("moshi == null");
        return new MoshiConverter(moshi, false, false, false);
    }

    /**
     * Return a new factory which uses {@linkplain JsonAdapter#lenient() lenient} adapters.
     */
    public MoshiConverter asLenient() {
        return new MoshiConverter(moshi, true, failOnUnknown, serializeNulls);
    }

    /**
     * Return a new factory which uses {@link JsonAdapter#failOnUnknown()} adapters.
     */
    public MoshiConverter failOnUnknown() {
        return new MoshiConverter(moshi, lenient, true, serializeNulls);
    }

    /**
     * Return a new factory which includes null values into the serialized JSON.
     */
    public MoshiConverter withNullSerialization() {
        return new MoshiConverter(moshi, lenient, failOnUnknown, true);
    }

    @Override
    public <T> T convert(ResponseBody body, Type type, boolean onResultDecoder) throws IOException {
        JsonAdapter<T> adapter = moshi.adapter(type);
        if (lenient) {
            adapter = adapter.lenient();
        }
        if (failOnUnknown) {
            adapter = adapter.failOnUnknown();
        }
        if (serializeNulls) {
            adapter = adapter.serializeNulls();
        }

        BufferedSource source = body.source();
        try {
            // Moshi has no document-level API so the responsibility of BOM skipping falls to whatever
            // is delegating to it. Since it's a UTF-8-only library as well we only honor the UTF-8 BOM.
            if (source.rangeEquals(0, UTF8_BOM)) {
                source.skip(UTF8_BOM.size());
            }
            JsonReader reader = JsonReader.of(source);
            T result = adapter.fromJson(reader);
            if (reader.peek() != JsonReader.Token.END_DOCUMENT) {
                throw new JsonDataException("JSON document was not fully consumed.");
            }
            return result;
        } finally {
            body.close();
        }
    }
}
