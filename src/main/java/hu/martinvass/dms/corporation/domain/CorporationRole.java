package hu.martinvass.dms.corporation.domain;

/**
 * Roles within a corporation.
 * Hierarchy: ADMIN > EMPLOYEE > GUEST
 */
public enum CorporationRole {

    /**
     * Corporation administrator with full access to all documents and settings.
     * Automatically has ADMIN permission on all documents.
     */
    ADMIN,

    /**
     * Employee with access to own documents and explicitly shared documents.
     * Can upload documents and has full control over their own documents.
     */
    EMPLOYEE,

    /**
     * Guest user with no upload rights.
     * Only has access to explicitly shared documents.
     */
    GUEST
}