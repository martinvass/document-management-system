package hu.martinvass.dms.document.domain;

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