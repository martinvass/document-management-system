package hu.martinvass.dms.storage;

public record StoredFile(
        String path,
        long size
) {}