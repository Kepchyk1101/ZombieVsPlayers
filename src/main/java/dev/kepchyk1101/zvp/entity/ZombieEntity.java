package dev.kepchyk1101.zvp.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "zombies")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZombieEntity {
  
  @DatabaseField(id = true)
  UUID uuid;
  
}
