package co.touchlab.andclass.cc;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/14/11
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatMessage implements Serializable
{
    public enum Type
    {
        Join, Leave, Message
    }
    Type type;
    String nickname;
    String post;

    public ChatMessage(Type type, String nickname, String post)
    {
        this.type = type;
        this.nickname = nickname;
        this.post = post;
    }

    public String getNickname()
    {
        return nickname;
    }

    public String getPost()
    {
        return post;
    }

    public Type getType()
    {
        return type;
    }
}
