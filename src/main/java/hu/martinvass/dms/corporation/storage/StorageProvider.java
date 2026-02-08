package hu.martinvass.dms.corporation.storage;

import hu.martinvass.dms.corporation.settings.storage.StorageType;

import java.io.IOException;
import java.io.InputStream;

public interface StorageProvider {
    StorageType supports();

    StoredFile save(
            Long companyId,
            String logicalPath,
            InputStream content,
            String contentType
    ) throws IOException;

    InputStream load(Long companyId, String logicalPath) throws IOException;

    void delete(Long companyId, String logicalPath) throws IOException;
}