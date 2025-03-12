package bord.projeto.persistence.converter;

import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OffsetDateTimeConverter {

    // Converte um Timestamp para OffsetDateTime, se não for nulo
    public static OffsetDateTime toOffsetDateTime(final Timestamp value) {
        return nonNull(value) ? OffsetDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC) : null;
    }

    // Converte um OffsetDateTime para Timestamp, se não for nulo
    public static Timestamp toTimestamp(final OffsetDateTime value) {
        return nonNull(value) ? Timestamp.valueOf(value.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()) : null;
    }

}