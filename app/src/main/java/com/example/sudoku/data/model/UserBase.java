// Relative Path: app/src/main/java/com/example/sudoku/data/model/UserBase.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Implement Serializable if needed for passing via Intent

public class UserBase implements Serializable { // Make Serializable if passing whole object

    @SerializedName("id")
    private String id; // Keep as String as UUIDs are often serialized as strings

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email; // Included as per backend schema, but might not be displayed

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Setters (Optional)
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
