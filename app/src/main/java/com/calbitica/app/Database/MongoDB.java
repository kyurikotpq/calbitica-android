package com.calbitica.app.Database;

import com.google.gson.annotations.SerializedName;

public class MongoDB {
//    Sample
//    {
//        "postId": 1,
//        "userId": 1,
//            "id": 1,
//          "name": "id labore ex et quam laborum",
//         "email": "Eliseo@gardner.biz",
//         "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
//          "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
//    },

    private int postId;

    private int userId;

    private Integer id;

    private String name;

    private String email;

    private String title;

    @SerializedName("body")     // Basically change the "text" to "body" as the key
    private String text;

    public MongoDB(int userId, String title, String text) {     // To fill up the input details like Add and Update
        this.userId = userId;
        this.title = title;
        this.text = text;
    }

    public int getPostId() { return postId; }

    public int getUserId() { return userId; }

    public Integer getId() { return id; }

    public String getTitle() { return title; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getText() { return text; }
}
