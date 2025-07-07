package org.kiril.notesapi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String NOTES_CACHE = "notes";
    public static final String NOTE_CACHE = "note";
    public static final String USERS_CACHE = "users";
}
