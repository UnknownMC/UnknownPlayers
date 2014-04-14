package com.mojang.api.profiles;

public interface ProfileRepository {
    public Profile[] findProfilesByCriteria(String... names);
}
