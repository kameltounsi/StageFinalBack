// src/main/java/com/esprit/stageback/storage/StorageService.java
package com.esprit.stageback.storage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface StorageService {
    /**
     * Upload a binary stream at the given key.
     *
     * @param key          object key (e.g. "trainer/12/group-3/2025/09/uuid.pdf")
     * @param contentType  MIME type (e.g. "application/pdf")
     * @param contentLength size in bytes (if unknown pass -1, but Wasabi/AWS préfère la valeur exacte)
     * @param stream       data stream (will NOT be closed by the impl)
     */
    void upload(String key, String contentType, long contentLength, InputStream stream);

    /** Delete an object by key (no error if missing). */
    void delete(String key);

    /** Generate a time-limited GET URL for downloading. */
    URL presignGet(String key, Duration expiresIn);
}
