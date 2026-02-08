package hu.martinvass.dms.corporation.storage.impl;

import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.storage.StoredFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ManagedStorageProvider implements StorageProvider {

    private static final Path ROOT = Paths.get("/var/dms/storage");

    @Override
    public StorageType supports() {
        return StorageType.MANAGED;
    }

    @Override
    public StoredFile save(
            Long companyId,
            String path,
            InputStream content,
            String contentType
    ) throws IOException {
        Path target = ROOT
                .resolve(String.valueOf(companyId))
                .resolve(path);

        Files.createDirectories(target.getParent());

        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);

        return new StoredFile(path, Files.size(target));
    }

    @Override
    public InputStream load(Long companyId, String path) throws IOException {
        return Files.newInputStream(
                ROOT.resolve(String.valueOf(companyId)).resolve(path)
        );
    }

    @Override
    public void delete(Long companyId, String path) throws IOException {
        Files.deleteIfExists(
                ROOT.resolve(String.valueOf(companyId)).resolve(path)
        );
    }
}
