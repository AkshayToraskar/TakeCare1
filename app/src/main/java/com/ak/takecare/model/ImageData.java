package com.ak.takecare.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dg hdghfd on 08-01-2018.
 */

public class ImageData extends RealmObject {

    @PrimaryKey
    long id;
    String prevPath, editedPath, DateTime;
    int age;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPrevPath() {
        return prevPath;
    }

    public void setPrevPath(String prevPath) {
        this.prevPath = prevPath;
    }

    public String getEditedPath() {
        return editedPath;
    }

    public void setEditedPath(String editedPath) {
        this.editedPath = editedPath;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
