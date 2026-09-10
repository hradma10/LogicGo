package cz.logicgo.core.entity.setting;


import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.util.converters.settings.SettingsNameConverter;
import cz.logicgo.core.util.converters.settings.SettingsValueHelper;
import jakarta.persistence.*;

import java.util.Objects;

@MappedSuperclass
public abstract class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "setting_name")
    @Convert(converter = SettingsNameConverter.class)
    private SettingKey key;

    @Column(nullable = false, name = "setting_value", length = 16384)
    private String value;

    public Setting() {
    }

    public Setting(SettingKey key, Object typedValue) {
        this.key = key;
        setTypedValue(typedValue);
    }

    public Setting(SettingKey key, String rawValue) {
        this.key = key;
        this.value = rawValue;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public SettingKey getKey() {
        return key;
    }

    public void setKey(SettingKey key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    public Object getTypedValue() {
        if (key == null || value == null) return null;
        return SettingsValueHelper.deserialize(this.value, key.getClazz());
    }

    @Transient
    public void setTypedValue(Object object) {
        this.value = SettingsValueHelper.serialize(object);
    }


    public abstract long getOwnerId();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Setting setting)) return false;
        return Objects.equals(getId(), setting.getId()) &&
                Objects.equals(getKey(), setting.getKey()) &&
                Objects.equals(getValue(), setting.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getKey(), getValue());
    }
}
