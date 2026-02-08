package hu.martinvass.dms.corporation.storage;

public record StoredFile(
        String path,
        long size
) {}