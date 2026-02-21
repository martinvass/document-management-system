package hu.martinvass.dms.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityType {

    DOCUMENT_UPLOADED("uploaded"),
    DOCUMENT_DOWNLOADED("downloaded"),
    DOCUMENT_ARCHIVED("archived"),
    NEW_VERSION_UPLOADED("uploaded new version of"),
    DOCUMENT_UPDATED("updated"),

    USER_JOINED("joined"),
    USER_LEFT("left"),
    USER_ROLE_CHANGED("changed role for"),

    PERMISSION_GRANTED("granted permission to"),
    PERMISSION_REVOKED("revoked permission from"),

    DEPARTMENT_CREATED("created department"),
    DEPARTMENT_ARCHIVED("archived department"),
    USER_ADDED_TO_DEPT("added user to department"),
    USER_REMOVED_FROM_DEPT("removed user from department");

    private final String displayText;
}