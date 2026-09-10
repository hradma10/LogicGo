package cz.logicgo.core.entity.export;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.PageLayout;
import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.core.util.converters.PageLayoutConverter;
import cz.logicgo.core.util.converters.PreviewTypeConverter;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "export_details")
@NamedQueries({
        @NamedQuery(
                name = "ExportDetail.getByUser",
                query = "SELECT ed FROM ExportDetail ed WHERE ed.user = :user"
        ),
        @NamedQuery(
                name = "ExportDetail.deleteById",
                query = "DELETE ExportDetail ed WHERE ed.id = :id"
        )
})
public class ExportDetail {

    public ExportDetail() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "export_games_link",
            joinColumns = @JoinColumn(name = "export_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<Game> exportedGames;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "last_download")
    private ZonedDateTime lastDownload;

    @Column(name = "path_export")
    private String pathToAssocFileExport;

    @Column(name = "path_user")
    private String pathToAssocFileNormal;

    @Column(name = "game_count")
    private int countOfGames;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "page_layout", nullable = false)
    @Convert(converter = PageLayoutConverter.class)
    private PageLayout pageLayout;

    @Column(name = "preview_type", nullable = false)
    @Convert(converter = PreviewTypeConverter.class)
    private PreviewType previewType = PreviewType.BOTH;

    @PrePersist
    public void prePersist() {
        createdAt = ZonedDateTime.now();
        countOfGames = exportedGames.size();
    }


    public long getId() {
        return id;
    }

    public ExportDetail setId(long id) {
        this.id = id;
        return this;
    }

    public int getCountOfGames() {
        return countOfGames;
    }

    public ExportDetail setCountOfGames(int countOfGames) {
        this.countOfGames = countOfGames;
        return this;
    }

    public List<Game> getExportedGames() {
        return exportedGames;
    }

    public ExportDetail setExportedGames(List<Game> exportedGames) {
        this.exportedGames = exportedGames;
        this.countOfGames = this.exportedGames.size();
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ExportDetail setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ZonedDateTime getLastDownload() {
        return lastDownload;
    }

    public ExportDetail setLastDownload(ZonedDateTime lastDownload) {
        this.lastDownload = lastDownload;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ExportDetail setUser(User user) {
        this.user = user;
        return this;
    }

    public PreviewType getPreviewType() {
        return previewType;
    }

    public ExportDetail setPreviewType(PreviewType previewType) {
        this.previewType = previewType;
        return this;
    }

    public PageLayout getPageLayout() {
        return pageLayout;
    }

    public ExportDetail setPageLayout(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
        return this;
    }

    public String getPathToAssocFileExport() {
        return pathToAssocFileExport;
    }

    public ExportDetail setPathToAssocFileExport(String pathToAssocFileExport) {
        this.pathToAssocFileExport = pathToAssocFileExport;
        return this;
    }

    public String getPathToAssocFileNormal() {
        return pathToAssocFileNormal;
    }

    public ExportDetail setPathToAssocFileNormal(String pathToAssocFileNormal) {
        this.pathToAssocFileNormal = pathToAssocFileNormal;
        return this;
    }
}
