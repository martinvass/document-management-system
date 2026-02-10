package hu.martinvass.dms.corporation.document.domain;

/**
 * Document-level permission levels.
 * Hierarchy: ADMIN > DELETE > EDIT > READ > NONE
 */
public enum DocumentPermissionLevel {

    /**
     * No access to the document
     */
    NONE,

    /**
     * Can view and download the document
     */
    READ,

    /**
     * Can upload new versions and modify metadata
     */
    EDIT,

    /**
     * Can delete or archive the document
     */
    DELETE,

    /**
     * Full access including permission management for other users
     */
    ADMIN;

    public boolean includes(DocumentPermissionLevel level) {
        return this.ordinal() >= level.ordinal();
    }

    /**
     * Check if this permission allows editing the document.
     */
    public boolean canRead() {
        return this.includes(READ);
    }

    /**
     * Check if this permission allows editing the document.
     */
    public boolean canEdit() {
        return this.includes(EDIT);
    }

    /**
     * Check if this permission allows deleting the document.
     */
    public boolean canDelete() {
        return this.includes(DELETE);
    }

    /**
     * Check if this permission allows managing permissions.
     */
    public boolean canManagePermissions() {
        return this == ADMIN;
    }
}