package hu.martinvass.dms.corporation.storage.impl;

import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.storage.StoredFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

// TODO: implement
@Service
public class AwsStorageProvider implements StorageProvider {

    @Override
    public StorageType supports() {
        return null;
    }

    @Override
    public StoredFile save(Long companyId, String logicalPath, InputStream content, String contentType) throws IOException {
        return null;
    }

    @Override
    public InputStream load(Long companyId, String logicalPath) throws IOException {
        return null;
    }

    @Override
    public void delete(Long companyId, String logicalPath) throws IOException {

    }
}