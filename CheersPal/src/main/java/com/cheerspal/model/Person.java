package com.cheerspal.model;

import java.io.Serializable;

public class Person implements Serializable
{
    public String firstName;
    public String lastName;
    public String id;

    public Person(String firstName, String lastName, String id)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }
}
