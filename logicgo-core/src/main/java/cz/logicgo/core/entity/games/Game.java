package cz.logicgo.core.entity.games;


import cz.logicgo.core.GradedGame;
import cz.logicgo.core.builders.GameCreation;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.Utils;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.util.converters.*;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


@NamedQueries({
        @NamedQuery(
                name = "Game.gamesWithSeedExistsForPlayer",
                query = "SELECT g FROM Game g WHERE g.seed = :seed and g.player = :player and g.forExport = false"
        ),
        @NamedQuery(
                name = "Game.gamesWithSeedExistsFinishedForPlayer",
                query = "SELECT count(g) as count FROM Game g WHERE g.seed = :seed and g.player = :player and g.status = :status"
        ),
        @NamedQuery(
                name = "Game.gamesPlayed",
                query = "SELECT count(g) as count FROM Game g WHERE g.player = :user and g.forExport = false"
        ),
        @NamedQuery(
                name = "Game.gamesByStatusCount",
                query = "SELECT count(g) as count FROM Game g WHERE g.player = :user and g.status = :status and g.forExport = false"
        ),
        @NamedQuery(
                name = "Game.updateGameNotes",
                query = "UPDATE Game g SET g.notes = :note WHERE g.id = :id"
        ),
        @NamedQuery(
                name = "Game.getGameNotes",
                query = "SELECT g.notes FROM Game g WHERE g.id = :id"
        ),
        @NamedQuery(
                name = "Game.updateThumbnailName",
                query = "UPDATE Game g SET g.thumbnailName = :name WHERE g.id = :id"
        ),
        @NamedQuery(
                name = "Game.getThumbnailName",
                query = "SELECT g.thumbnailName FROM Game g WHERE g.id = :id"
        ),
        @NamedQuery(
                name = "Game.deleteById",
                query = "DELETE Game g WHERE g.id = :id"
        ),
        @NamedQuery(
                name = "Game.lastPlayedNonFinishedGame",
                query = "SELECT g FROM Game g WHERE g.status = :status and g.player = :player and g.forExport = false ORDER BY g.lastPlayed DESC"
        ),
        @NamedQuery(
                name = "Game.getAllThumbnailPaths",
                query = "SELECT g.thumbnailName FROM Game g"
        )
})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Game implements GradedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id")
    private Long id;

    @Column(nullable = false, name = "height")
    private int height;

    @Column(nullable = false, name = "width")
    private int width;

    @Column(name = "finished_at")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime finishedAt;

    @Transient
    private Random randomInstance;

    @Column(nullable = false, name = "seed")
    private long seed;

    @Column(nullable = false, name = "date_created")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime creationDate;

    @Column(nullable = false, name = "game_type")
    @Convert(converter = GameTypeConverter.class)
    private TypeGame typeGame;

    @Column(nullable = false, name = "last_played")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime lastPlayed;

    @Column(nullable = false, name = "status")
    @Convert(converter = StatusConverter.class)
    private Status status;

    @Column(nullable = false, name = "elapsed_time")
    private Duration elapsedTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Convert(converter = NotesConverter.class)
    private String notes;

    @Column(nullable = false, name = "difficulty")
    @Convert(converter = DifficultyConverter.class)
    private Difficulty difficulty;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private User player;

    @Column(name = "thumbnail_name")
    private String thumbnailName;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "undo_stack")
    private byte[] undoStack;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "redo_stack")
    private byte[] redoStack;

    @Column(name = "for_export", nullable = false)
    private boolean forExport = false;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSetting> settings;

    @Transient
    private transient int grade = 0;

    public void prePersist() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }

        if (lastPlayed == null) {
            lastPlayed = LocalDateTime.now();
        }

        if (elapsedTime == null) {
            elapsedTime = Duration.ZERO;
        }

        if (status == null) {
            status = Status.IN_PROGRESS;
        }

        if (undoStack == null) {
            undoStack = new byte[0];
        }
        if (redoStack == null) {
            redoStack = new byte[0];
        }

    }

    public void preUpdate() {
        lastPlayed = LocalDateTime.now();
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public Game() {
        randomInstance = new Random(seed);
    }

    public Game(GameCreation<?> creation) {
        this.height = creation.getHeight();
        this.width = creation.getWidth();
        this.seed = creation.getSeed();
        this.randomInstance = new Random(seed);
        this.status = Status.IN_PROGRESS;
        this.typeGame = creation.getTypeGame();
        this.elapsedTime = Duration.ZERO;
        this.difficulty = creation.getDifficulty();
        this.player = creation.getPlayer();
    }

    public Game(GameCreation<?> creation, byte[] undoStack, byte[] redoStack) {
        this.undoStack = undoStack != null ? undoStack.clone() : null;
        this.redoStack = redoStack != null ? redoStack.clone() : null;
        this(creation);
    }

    public Game(Game game) {
        this.undoStack = game.getUndoStack() != null ? game.getUndoStack().clone() : null;
        this.redoStack = game.getRedoStack() != null ? game.getRedoStack().clone() : null;

        this.height = game.getHeight();
        this.width = game.getWidth();
        this.seed = game.getSeed();
        this.difficulty = game.getDifficulty();
        this.elapsedTime = game.getElapsedTime();
        this.status = game.getStatus();
        this.forExport = game.isForExport();

        this.creationDate = game.getCreationDate();
        this.typeGame = game.getGameType();
        this.thumbnailName = game.getThumbnailName();
        this.lastPlayed = game.getLastPlayed();

        this.player = game.getPlayer();
        this.notes = game.getNotes();

        this.randomInstance = new Random(seed);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Random getRandomInstance() {
        return randomInstance;
    }

    public long getSeed() {
        return seed;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeGame getGameType() {
        return typeGame;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setRandomInstance(Random randomInstance) {
        this.randomInstance = randomInstance;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setGameType(TypeGame typeGame) {
        this.typeGame = typeGame;
    }

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    @Transient
    public String getFormattedLastPlayed() {
        return Utils.dateTimeFormatter(lastPlayed);
    }

    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<GameSetting> getSettings() {
        return settings;
    }

    public List<GameSetting> getSettingsAsMap() {
        return settings;
    }


    public void setSettings(List<GameSetting> settings) {
        this.settings = settings;
    }

    public String getThumbnailName() {
        return thumbnailName;
    }

    public void setThumbnailName(String thumbnailName) {
        this.thumbnailName = thumbnailName;
    }

    public abstract GameType getTypeOfGame();

    public byte[] getUndoStack() {
        return undoStack;
    }

    public void setUndoStack(byte[] undoStack) {
        this.undoStack = undoStack;
    }

    public byte[] getRedoStack() {
        return redoStack;
    }

    public void setRedoStack(byte[] redoStack) {
        this.redoStack = redoStack;
    }


    public boolean isForExport() {
        return forExport;
    }

    public Game setForExport(boolean forExport) {
        this.forExport = forExport;
        return this;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public Game setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }
}
