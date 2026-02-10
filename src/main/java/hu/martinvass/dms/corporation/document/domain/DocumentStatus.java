package hu.martinvass.dms.corporation.document.domain;

/**
 * Status of a document.
 */
public enum DocumentStatus {

    /**
     * Document is active and accessible
     */
    ACTIVE,

    /**
     * Document is archived (soft deleted)
     */
    ARCHIVED,

    /**
     * Document is permanently deleted
     */
    DELETED
}