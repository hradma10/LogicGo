package cz.logicgo.core.entity.user;


import cz.logicgo.core.GameUtils;
import cz.logicgo.core.entity.setting.UserSetting;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.gameClasses.viewers.CustomMask;
import cz.logicgo.core.gameClasses.viewers.SudokuPattern;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.util.converters.ColorListConverter;
import cz.logicgo.core.util.converters.CustomMaskListConverter;
import cz.logicgo.core.util.converters.GameFavoriteListConverter;
import cz.logicgo.core.util.converters.SudokuPatternListConverter;
import cz.logicgo.core.util.converters.settings.KeyEventConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "User.findByUsername",
                query = "SELECT u FROM User u WHERE u.username = :username"
        ),
        @NamedQuery(
                name = "User.findById",
                query = "SELECT u FROM User u WHERE u.id = :id"
        ),
        @NamedQuery(
                name = "User.findPasswordHashByUsername",
                query = "SELECT u.hashedPassword FROM User u WHERE u.username = :username"
        ),
        @NamedQuery(
                name = "User.findAnon",
                query = "SELECT u FROM User u WHERE u.hashedPassword = ''"
        ),
        @NamedQuery(
                name = "User.findAll",
                query = "SELECT u FROM User u"
        ),
        @NamedQuery(
                name = "User.countAll",
                query = "SELECT COUNT(u) FROM User u"
        ),
})
@Table(name = "users")
public class User {

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<UserSetting> userSettings = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private String hashedPassword;
    @Column(nullable = false)
    private boolean hasPassword = false;
    @Column(name = "last_logged", nullable = false)
    private LocalDateTime lastLogged;

    @Convert(converter = SudokuPatternListConverter.class)
    @Column(name = "custom_patterns_data", columnDefinition = "BLOB")
    private List<SudokuPattern> customPatterns = new ArrayList<>();

    @Convert(converter = CustomMaskListConverter.class)
    @Column(name = "custom_masks_data", columnDefinition = "BLOB")
    private List<CustomMask> customMasks = new ArrayList<>();
    @Convert(converter = ColorListConverter.class)
    @Column(name = "user_colors", columnDefinition = "TEXT")
    private List<String> userColors = new ArrayList<>();
    @Convert(converter = KeyEventConverter.class)
    @Column(name = "saved_keys", nullable = false)
    private HashMap<HotkeyEvent, KeyEventDTO> savedHotkeys = new HashMap<>();
    @Convert(converter = GameFavoriteListConverter.class)
    @Column(name = "favorite_comb", nullable = false)
    private List<GameFavorite> favoriteComb = new ArrayList<>();

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public User() {
    }

    public List<SudokuPattern> getCustomPatterns() {
        return customPatterns;
    }

    public User setCustomPatterns(List<SudokuPattern> customPatterns) {
        this.customPatterns = customPatterns;
        return this;
    }

    public List<CustomMask> getCustomMasks() {
        return customMasks;
    }

    public User setCustomMasks(List<CustomMask> customMasks) {
        this.customMasks = customMasks;
        return this;
    }

    public List<String> getUserColors() {
        if (userColors == null || userColors.isEmpty()) {
            userColors = GameUtils.getDefaultColorTheme();
        }
        return userColors;
    }

    public User setUserColors(List<String> userColors) {
        this.userColors = userColors;
        return this;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastLogged == null) {
            lastLogged = LocalDateTime.now();
        }
        if (userColors == null || userColors.isEmpty()) {
            userColors = GameUtils.getDefaultColorTheme();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogged() {
        return lastLogged;
    }

    public void setLastLogged(LocalDateTime lastLogged) {
        this.lastLogged = lastLogged;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public User setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
        return this;
    }

    public List<UserSetting> getUserSettings() {
        return userSettings;
    }


    public HashMap<HotkeyEvent, KeyEventDTO> getSavedHotkeys() {
        return savedHotkeys;
    }

    public void addSavedKey(KeyEventDTO keyEventDTO) {
        if (savedHotkeys == null) savedHotkeys = new HashMap<>();

        HotkeyEvent event = keyEventDTO.getKeystrokeEvent();
        savedHotkeys.remove(event);
        savedHotkeys.put(event, keyEventDTO);
    }

    public List<GameFavorite> getFavoriteComb() {
        return favoriteComb;
    }

    public User setFavoriteComb(List<GameFavorite> favoriteComb) {
        this.favoriteComb = favoriteComb;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(getUsername(), user.getUsername()) && Objects.equals(getHashedPassword(), user.getHashedPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getHashedPassword());
    }
}
